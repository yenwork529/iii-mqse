package org.iii.esd.api.vo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.LastModifiedDate;

import org.iii.esd.enums.DataType;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.integrate.DrResData;
import org.iii.esd.mongo.document.integrate.GessResData;
import org.iii.esd.utils.MathUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ResElectricData {

    private UUID id;

    private FieldProfile fieldProfile;

    private String resId;

    /**
     * 統計資料時間
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date time;

    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date updateTime;

    /**
     * 資料類型
     */
    @Enumerated(EnumType.STRING)
    private DataType dataType;

    /**
     * 第一電錶功率
     */
    @Builder.Default
    private BigDecimal meter1kW = BigDecimal.ZERO;

    /**
     * 第二電錶功率
     */
    @Builder.Default
    private BigDecimal meter2kW = BigDecimal.ZERO;

    /**
     * 第三電錶功率
     */
    @Builder.Default
    private BigDecimal meter3kW = BigDecimal.ZERO;

    /**
     * 第四電錶功率
     */
    @Builder.Default
    private BigDecimal meter4kW = BigDecimal.ZERO;

    /**
     * 原始負載15分鐘需量
     */
    @Builder.Default
    private BigDecimal m0kW = BigDecimal.ZERO;

    /**
     * 實際負載15分鐘需量
     */
    @Builder.Default
    private BigDecimal m1kW = BigDecimal.ZERO;

    /**
     * PV發電15分鐘需量
     */
    @Builder.Default
    private BigDecimal m2kW = BigDecimal.ZERO;

    /**
     * 電池發電15分鐘需量
     */
    @Builder.Default
    private BigDecimal m3kW = BigDecimal.ZERO;

    /**
     * 其他負載15分鐘需量
     */
    @Builder.Default
    private BigDecimal m5kW = BigDecimal.ZERO;

    /**
     * 可控設備負載15分鐘需量
     */
    @Builder.Default
    private BigDecimal m6kW = BigDecimal.ZERO;

    /**
     * 可控設備卸載15分鐘需量
     */
    @Builder.Default
    private BigDecimal m7kW = BigDecimal.ZERO;

    /**
     * 燃料電池發電15分鐘需量
     */
    @Builder.Default
    private BigDecimal m8kW = BigDecimal.ZERO;

    /**
     * 風機發電15分鐘需量
     */
    @Builder.Default
    private BigDecimal m9kW = BigDecimal.ZERO;

    /**
     * 發電機15分鐘需量
     */
    @Builder.Default
    private BigDecimal m10kW = BigDecimal.ZERO;

    /**
     * 躉售PV發電15分鐘需量
     */
    @Builder.Default
    private BigDecimal m20kW = BigDecimal.ZERO;

    /**
     * 當月最大需量
     */
    private BigDecimal trhd;

    /**
     * 該時段電池蓄電量
     */
    private BigDecimal msoc;

    /**
     * 該時段電池保留餘量
     */
    private BigDecimal msocRsv;

    /**
     * 電池回填餘量
     */
    private BigDecimal msocBackfill;

    /**
     * 銷峰門檻
     */
    private BigDecimal pks;

    /**
     * 該時間實際負載(M1)即時功率
     */
    private BigDecimal activePower;

    /**
     * 該時間場域總表累積度數 (Net)
     */
    private BigDecimal totalkWh;

    /**
     * 是否需要補值
     */
    private Boolean needFix;

    private BigDecimal energyImp; // 2021-0917

    private BigDecimal energyExp; // 2021-0917

    @Builder.Default
    private int defaultScale = 3;

    public void init(int... scale) {
        setScale(scale.length > 0 ? scale[0] : defaultScale);
        balance();
    }

    /**
     * 呼叫平衡公式
     */
    public void balance() {
        // 以後總表會裝在M1
        m0kW = m1kW.add(m2kW != null ? m2kW : BigDecimal.ZERO).
                   add(m3kW != null ? m3kW : BigDecimal.ZERO).
                   add(m7kW != null ? m7kW : BigDecimal.ZERO).
                   add(m8kW != null ? m8kW : BigDecimal.ZERO).
                   add(m9kW != null ? m9kW : BigDecimal.ZERO).
                   add(m10kW != null ? m10kW : BigDecimal.ZERO);
    }

    public void setScale() {
        setScale(defaultScale);
    }

    public ResElectricData sum(ResElectricData electricData) {
        return ResElectricData.builder()
                              .time(electricData.getTime())
                              .m0kW(MathUtils.sum(m0kW, electricData.getM0kW()))
                              .m1kW(MathUtils.sum(m1kW, electricData.getM1kW()))
                              .m2kW(MathUtils.sum(m2kW, electricData.getM2kW()))
                              .m3kW(MathUtils.sum(m3kW, electricData.getM3kW()))
                              .m5kW(MathUtils.sum(m5kW, electricData.getM5kW()))
                              .m6kW(MathUtils.sum(m6kW, electricData.getM6kW()))
                              .m7kW(MathUtils.sum(m7kW, electricData.getM7kW()))
                              .m8kW(MathUtils.sum(m8kW, electricData.getM8kW()))
                              .m9kW(MathUtils.sum(m9kW, electricData.getM9kW()))
                              .m10kW(MathUtils.sum(m10kW, electricData.getM10kW()))
                              .activePower(MathUtils.sum(activePower, electricData.getActivePower()))
                              .totalkWh(MathUtils.sum(totalkWh, electricData.getTotalkWh()))
                              .needFix((needFix != null ? needFix : false) || electricData.getNeedFix())
                              .build();
    }

    public static ResElectricData of(DrResData resData) {
        return ResElectricData.builder()
                              .time(resData.getTimestamp())
                              .resId(resData.getResId())
                              .dataType(DataType.T99)
                              .activePower(resData.getM1kW())
                              .meter1kW(resData.getM1kW())
                              .m0kW(resData.getM1kW())
                              .m1kW(resData.getM1kW())
                              .totalkWh(resData.getM1EnergyNET())
                              .energyExp(resData.getM1EnergyEXP())
                              .energyImp(resData.getM1EnergyIMP())
                              .msoc(resData.getDr1Status())
                              .needFix(false)
                              .build();
    }

    public static ResElectricData of(GessResData resData) {
        return ResElectricData.builder()
                              .time(resData.getTimestamp())
                              .resId(resData.getResId())
                              .dataType(DataType.T99)
                              .activePower(resData.getM1kW())
                              .meter1kW(resData.getM1kW())
                              .energyExp(resData.getM1EnergyEXP())
                              .energyImp(resData.getM1EnergyIMP())
                              .msoc(resData.getE1SOC())
                              .m3kW(resData.getM1kW())
                              .needFix(false)
                              .build();
    }

    /**
     * 計算回填餘量
     */
    public BigDecimal getMsocBackfill() {
        if (msoc != null) {
            msocBackfill = msoc.subtract(msocRsv != null ? msocRsv : BigDecimal.ZERO).max(BigDecimal.ZERO)
                               .setScale(defaultScale, BigDecimal.ROUND_HALF_UP);
        }
        return msocBackfill;
    }

    /**
     * 設定小數位數，四捨五入
     *
     * @param scale
     */
    public void setScale(int scale) {
        if (m0kW != null) {m0kW = m0kW.setScale(scale, BigDecimal.ROUND_HALF_UP);}
        if (m1kW != null) {m1kW = m1kW.setScale(scale, BigDecimal.ROUND_HALF_UP);} else {m1kW = BigDecimal.ZERO;}
        if (m2kW != null) {m2kW = m2kW.setScale(scale, BigDecimal.ROUND_HALF_UP);}
        if (m3kW != null) {m3kW = m3kW.setScale(scale, BigDecimal.ROUND_HALF_UP);}
        if (m5kW != null) {m5kW = m5kW.setScale(scale, BigDecimal.ROUND_HALF_UP);} else {m5kW = BigDecimal.ZERO;}
        if (m6kW != null) {m6kW = m6kW.setScale(scale, BigDecimal.ROUND_HALF_UP);}
        if (m7kW != null) {m7kW = m7kW.setScale(scale, BigDecimal.ROUND_HALF_UP);}
        if (m8kW != null) {m8kW = m8kW.setScale(scale, BigDecimal.ROUND_HALF_UP);}
        if (m9kW != null) {m9kW = m9kW.setScale(scale, BigDecimal.ROUND_HALF_UP);}
        if (m10kW != null) {m10kW = m10kW.setScale(scale, BigDecimal.ROUND_HALF_UP);}
        if (m20kW != null) {m20kW = m20kW.setScale(scale, BigDecimal.ROUND_HALF_UP);}
    }
}