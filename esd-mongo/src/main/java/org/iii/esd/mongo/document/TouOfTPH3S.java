package org.iii.esd.mongo.document;

import java.math.BigDecimal;
import java.util.Calendar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import org.iii.esd.enums.TouType;
import org.iii.esd.utils.DatetimeUtils;

@Document(collection = "TOUS")
@Getter
@Setter
@AllArgsConstructor
@ToString
@Builder
public class TouOfTPH3S extends AbstractTou {

    /**
     * 夏月經常契約
     */
    @Builder.Default
    BigDecimal Summer_Regular_Contarct = BigDecimal.ZERO;
    /**
     * 非夏月經常契約
     */
    @Builder.Default
    BigDecimal NonSummer_Regular_Contarct = BigDecimal.ZERO;
    /**
     * 夏月半尖峰契約
     */
    @Builder.Default
    BigDecimal Summer_HalfPeak_Contarct = BigDecimal.ZERO;
    /***
     * 非夏月半尖峰契約
     */
    @Builder.Default
    BigDecimal NonSummer_HalfPeak_Contarct = BigDecimal.ZERO;
    /***
     * 夏月週六半尖峰契約
     */
    @Builder.Default
    BigDecimal Summer_SaturdayHalfPeak_Contarct = BigDecimal.ZERO;
    /***
     * 非夏月週六半尖峰契約
     */
    @Builder.Default
    BigDecimal NonSummer_SaturdayHalfPeak_Contarct = BigDecimal.ZERO;
    /***
     * 夏月離峰契約
     */
    @Builder.Default
    BigDecimal Summer_OffPeak_Contarct = BigDecimal.ZERO;
    /***
     * 非夏月離峰契約
     */
    @Builder.Default
    BigDecimal NonSummer_OffPeak_Contarct = BigDecimal.ZERO;
    /***
     * 夏月平日尖峰時段
     */
    @Builder.Default
    BigDecimal Summer_NormalDay_Peak = BigDecimal.ZERO;
    /***
     * 夏月平日半尖峰時段
     */
    @Builder.Default
    BigDecimal Summer_NormalDay_HalfPeak = BigDecimal.ZERO;
    /***
     * 非夏月平日尖峰時段
     */
    @Builder.Default
    BigDecimal NonSummer_NormalDay_HalfPeak = BigDecimal.ZERO;
    /***
     * 夏月平日離峰時段
     */
    @Builder.Default
    BigDecimal Summer_NormalDay_OffPeak = BigDecimal.ZERO;
    /***
     * 非夏月平日離峰時段
     */
    @Builder.Default
    BigDecimal NonSummer_NormalDay_OffPeak = BigDecimal.ZERO;
    /***
     * 夏月週六半尖峰時間
     */
    @Builder.Default
    BigDecimal Summer_Saturday_HalfPeak = BigDecimal.ZERO;
    /***
     * 非夏月週六半尖峰時間
     */
    @Builder.Default
    BigDecimal NonSummer_Saturday_HalfPeak = BigDecimal.ZERO;
    /***
     * 夏月週六離峰時間
     */
    @Builder.Default
    BigDecimal Summer_Saturday_OffPeak = BigDecimal.ZERO;
    /***
     * 非夏月週六離峰時間
     */
    @Builder.Default
    BigDecimal NonSummer_Saturday_OffPeak = BigDecimal.ZERO;
    /***
     * 夏月週日及離峰日離峰時間
     */
    @Builder.Default
    BigDecimal Summer_OffPeakDay_OffPeak = BigDecimal.ZERO;
    /***
     * 非夏月週日及離峰日離峰時間
     */
    @Builder.Default
    BigDecimal NonSummer_OffPeakDay_OffPeak = BigDecimal.ZERO;
    /***
     * 功率因數調整費用扣減比例
     */
    @Builder.Default
    BigDecimal PF_Adj = BigDecimal.ZERO;

    public TouOfTPH3S() {
        this.type = TouType.TPH3S;
    }

    /**
     * 取得時間電價並搭配尖離峰電價倍率
     */
    @Override
    public BigDecimal currentPriceOfTime(Calendar date, double hours, BigDecimal peakRatio,
            BigDecimal offPeakRatio) {

        boolean isSaturday = date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY;

        if (isSummerMonth(date)) {

            if (DatetimeUtils.isHoliday(date)) {
                return this.Summer_OffPeakDay_OffPeak.multiply(offPeakRatio);
            } else if (isSaturday) {
                if (hours > 7.5 && hours <= 22.5) {
                    return this.Summer_Saturday_HalfPeak.multiply(peakRatio);
                } else {
                    return this.Summer_Saturday_OffPeak.multiply(offPeakRatio);
                }
            } else {
                if (hours > 10 && hours <= 12) {
                    return this.Summer_NormalDay_Peak.multiply(peakRatio);
                } else if (hours > 12 && hours <= 13) {
                    return Summer_NormalDay_HalfPeak.multiply(peakRatio);
                } else if (hours > 13 && hours <= 17) {
                    return this.Summer_NormalDay_Peak.multiply(peakRatio);
                } else if (hours > 7.5 && hours <= 10) {
                    return this.Summer_NormalDay_HalfPeak.multiply(peakRatio);
                } else if (hours > 17 && hours <= 22.5) {
                    return this.Summer_NormalDay_HalfPeak.multiply(peakRatio);
                } else {
                    return Summer_NormalDay_OffPeak.multiply(offPeakRatio);
                }
            }
        } else {
            if (DatetimeUtils.isHoliday(date)) {
                return this.NonSummer_OffPeakDay_OffPeak.multiply(offPeakRatio);
            } else if (isSaturday) {
                if (hours > 7.5 && hours <= 22.5) {
                    return this.NonSummer_Saturday_HalfPeak.multiply(peakRatio);
                } else {
                    return this.NonSummer_Saturday_OffPeak.multiply(offPeakRatio);
                }
            } else {
                if (hours > 7.5 && hours <= 22.5) {
                    return this.NonSummer_NormalDay_HalfPeak.multiply(peakRatio);
                } else {
                    return NonSummer_NormalDay_OffPeak.multiply(offPeakRatio);
                }
            }
        }
    }

}
