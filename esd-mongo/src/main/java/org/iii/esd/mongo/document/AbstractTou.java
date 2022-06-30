package org.iii.esd.mongo.document;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.iii.esd.enums.TouType;
import org.iii.esd.utils.DatetimeUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public abstract class AbstractTou extends SequenceDocument {

    protected static final BigDecimal MINUTES_60 = BigDecimal.valueOf(60);

    /**
     * 電費類型
     */
    protected TouType type;
    protected Date activeTime;

    public abstract BigDecimal currentPriceOfTime(Calendar date, double hours, BigDecimal peakRatio,
            BigDecimal offPeakRatio);

    public BigDecimal currentPriceOfTime(Date time) {
        return currentPriceOfTime(time, BigDecimal.ONE, BigDecimal.ONE);
    }

    public BigDecimal currentPriceOfTime(Date time, BigDecimal peakRatio, BigDecimal offPeakRatio) {
        // 先用原本給的時間計算當天過了多少分鐘
        long minutes = ChronoUnit.MINUTES.between(DatetimeUtils.getFirstHourOfDay(time).toInstant(), time.toInstant());
        // 將分鐘轉成小時判斷
        double hours = BigDecimal.valueOf(minutes).divide(MINUTES_60, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        // 所以-15分鐘讓日期還算是前一天，但是時段的計算上還是用原本的日期
        // 時間為什麼要-15分鐘是因為這樣比較好判斷當天，因為每天最後一筆是2400，已經跨日
        Calendar dateStart = Calendar.getInstance();
        dateStart.setTime(time);
        dateStart.add(Calendar.MINUTE, -15);
        return currentPriceOfTime(dateStart, hours, peakRatio, offPeakRatio);
    }

    public boolean isSummerMonth(Calendar SelectDate_Start) {
        int month = SelectDate_Start.get(Calendar.MONTH);
        return month >= Calendar.JUNE && month <= Calendar.SEPTEMBER;
    }

}