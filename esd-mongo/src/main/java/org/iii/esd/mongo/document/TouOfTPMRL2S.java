package org.iii.esd.mongo.document;

import java.math.BigDecimal;
import java.util.Calendar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import org.iii.esd.enums.TouType;
import org.iii.esd.exception.IiiException;
import org.iii.esd.utils.DatetimeUtils;

@Document(collection = "TOUS")
@Getter
@Setter
@AllArgsConstructor
@ToString
@Builder
public class TouOfTPMRL2S extends AbstractTou {

    /***
     * 基本電費、按戶計收(每戶每月)
     */
    @Builder.Default
    BigDecimal basic = BigDecimal.valueOf(75.00);
    /***
     * 每月總度數超過2000度的部分，每度加收0.96元
     */
    @Builder.Default
    BigDecimal overcost = BigDecimal.valueOf(0.96);
    /***
     * 夏月，6/1到9/30
     */
    @Builder.Default
    TPMRL2S_Season summer = new TPMRL2S_Season(4.44, 1.8, 1.8);
    /***
     * 非夏月，夏月以外的時間
     */
    @Builder.Default
    TPMRL2S_Season nonsummer = new TPMRL2S_Season(4.23, 1.73, 1.73);

    public TouOfTPMRL2S() {
        this.type = TouType.TPMRL2S;
    }

    /***
     * 取得時間電價並搭配尖離峰電價倍率
     */
    @Override
    public BigDecimal currentPriceOfTime(Calendar date, double hours, BigDecimal peakRatio,
            BigDecimal offPeakRatio) {

        boolean isSaturday = date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY;
        boolean isSunday = date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;

        TPMRL2S_Season useSeason = this.nonsummer;
        if (isSummerMonth(date)) {
            useSeason = this.summer;

        }
        // 夏季
        if (DatetimeUtils.isHoliday(date)) {
            return useSeason.h_peak.multiply(offPeakRatio);
        } else if (isSaturday || isSunday) {
            return useSeason.h_peak.multiply(offPeakRatio);
        } else if (hours <= 7.5 || hours > 22.5) {
            return useSeason.n_o_peak.multiply(offPeakRatio);
        } else if (hours > 7.5 && hours <= 22.5) {
            return useSeason.n_t_peak.multiply(peakRatio);
        } else {
            throw new IiiException("程式異常");
        }
    }

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class TPMRL2S_Season {
    /// <summary>
    /// 周一到周五 尖峰時段 0730-2230
    /// </summary>
    BigDecimal n_t_peak;
    /// <summary>
    /// 周一到周五 離峰時段 00:00-0730,2230-2400
    /// </summary>
    BigDecimal n_o_peak;
    /// <summary>
    /// 周六、周日、離峰日，全日價格
    /// </summary>
    BigDecimal h_peak;
    public TPMRL2S_Season(double n_t_peak, double n_o_peak, double h_peak) {
        this.n_t_peak = BigDecimal.valueOf(n_t_peak);
        this.n_o_peak = BigDecimal.valueOf(n_o_peak);
        this.h_peak = BigDecimal.valueOf(h_peak);
    }
}
