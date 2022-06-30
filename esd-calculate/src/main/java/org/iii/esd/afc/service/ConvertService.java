package org.iii.esd.afc.service;

import java.util.Arrays;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import org.iii.esd.afc.algorithm.ConvertMode;
import org.iii.esd.afc.algorithm.DefaultStrategy;
import org.iii.esd.afc.def.ZoneEnum;

import static org.iii.esd.afc.def.ZoneEnum.DEAD_BAND;
import static org.iii.esd.afc.utils.Calculator.getFrequencyLimit;
import static org.iii.esd.afc.utils.Calculator.isInverseRatioRelationship;

@Service
@Log4j2
public class ConvertService {
    /***
     * AFC(Automatic Frequency Control) Convert Service<br>
     * 1、頻率自動控制系統轉換模組。<br>
     * 2、Spec係依據20200116台電採購案簡報(p22)服務及操作規格定義進行系統頻率和輸出功率比例轉換。<br>
     * 3、演算法：除在不動帶考慮電池容量決定充或放電外，在其他區間採用內插法取中間值。<br>
     * @param frequency 系統頻率
     * @param soc 電池容量
     * @return powerRatio 輸出功率比例(%)
     */
    public Double run(Double frequency, Double soc) {
        log.debug("AFC Algorithm Converting...[frequency=" + frequency + ", soc=" + soc + "]");
        ConvertMode mode = new ConvertMode(new DefaultStrategy(getFrequencyLimit(frequency), soc));
        Double powerRatio = mode.execute();
        log.debug("AFC Algorithm Result [target power ratio=" + powerRatio + "]");
        return powerRatio;
    }

    /***
     * AFC(Automatic Frequency Control) Convert Service<br>
     * 1、頻率自動控制系統轉換模組。<br>
     * 2、Spec係依據20200116台電採購案簡報(p22)服務及操作規格定義進行系統頻率和輸出功率比例轉換。<br>
     * 3、演算法：除在不動帶考慮電池容量決定充或放電外，在其他區間採用內插法取中間值。<br>
     * 4、額外考量：不動帶以外區域，如果計算後的powerRatio(Pt1)導致(Pt1-Pt0)x(Ft1-Ft0)>0(正比關係)，為避免sbspm=0的狀況，則忽略原來得到的Pt1，直接回傳Pt0做為輸出功率比例<br>
     * @param frequencies 前一秒(t-1) & 這一秒(t)的系統頻率
     * @Param lastPowerRatio 這一秒(t)的輸出功率比例
     * @param soc 電池容量
     * @return powerRatio 下一秒(t+1)的輸出功率比例(%)
     */
    public Double run(Double[] frequencies, Double lastPowerRatio, Double soc) {
        if (lastPowerRatio == null || frequencies.length != 2 || frequencies[0] == null || frequencies[1] == null) {
            log.error("Fail to convert power ratio due to invalid input data...");
            return null;
        }

        log.debug("frequencies=" + Arrays.toString(frequencies));
        Double[] frequenciesLimit = getFrequencyLimitArray(frequencies);
        log.debug("frequenciesLimit=" + Arrays.toString(frequenciesLimit));

        Double powerRatio = run(frequenciesLimit[1], soc);
        ZoneEnum whichZone = ZoneEnum.of(frequenciesLimit[1]);

        if (powerRatio != null && whichZone != null) {
            Double[] powerRatios = {lastPowerRatio, powerRatio};
            if (whichZone != DEAD_BAND && !isInverseRatioRelationship(frequenciesLimit, powerRatios)) {
                log.info("if powerRatios=" + Arrays.toString(powerRatios) + "==>then (ΔPxΔf>0)==>so fix powerRatio(=lastPowerRatio)=" +
                        lastPowerRatio);
                powerRatio = lastPowerRatio;
            }
        }
        return powerRatio;
    }

    private Double[] getFrequencyLimitArray(Double[] frequencies) {
        Double[] frequenciesLimit = new Double[frequencies.length];
        for (int i = 0; i < frequenciesLimit.length; i++) {
            frequenciesLimit[i] = getFrequencyLimit(frequencies[i]);
        }
        return frequenciesLimit;
    }
}
