package org.iii.esd.enums;

import java.util.Calendar;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.iii.esd.utils.DatetimeUtils;

@Getter
@AllArgsConstructor
public enum DateType {
    /**
     * 周一到周四
     */
    D14(1),
    /**
     * 周五
     */
    D5(2),
    /**
     * 周六
     */
    D6(3),
    /**
     * 周日,假日
     */
    DH(5),
    /**
     * 上班日(補班日...等)
     */
    DW(6),
    /**
     * 太陽能
     */
    PV(99),
    /**
     * 混合型
     */
    MIXED(9999);

    private int value;

    public static DateType getDateType(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        if (DatetimeUtils.isHoliday(date)) {
            return DateType.DH;
        } else if (week == Calendar.SATURDAY) {
            return DateType.D6;
        } else if (week == Calendar.FRIDAY) {
            return DateType.D5;
        } else {
            return DateType.D14;
        }
    }

}