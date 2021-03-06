package org.iii.esd.caculate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.iii.esd.enums.PolicyDevice;
import org.iii.esd.enums.PolicyService;
import org.iii.esd.mongo.document.PolicyProfile;
import org.iii.esd.mongo.vo.Level3;

public class Utility {
    public static final int MINUTES_15 = 900;
    public static final int MINUTES_60 = 3600;

    /**
     * * 0.25
     */
    public static final BigDecimal toKwh = new BigDecimal(0.25);
    /**
     * * - 1
     */
    public static final BigDecimal minus = new BigDecimal(-1);
    /**
     * * 4
     */
    public static final BigDecimal kwhTokW = BigDecimal.valueOf(4);

    public static Calendar GetControlStart(Date Control_Start) {
        Calendar controlStart = Calendar.getInstance();
        controlStart.setTime(Control_Start);
        controlStart.set(Calendar.SECOND, 0);
        controlStart.set(Calendar.MILLISECOND, 0);
        return controlStart;
    }

    public static Calendar GetControlDay(Date Control_Start) {
        Calendar controlStart = Calendar.getInstance();
        controlStart.setTime(Control_Start);
        controlStart.set(Calendar.HOUR_OF_DAY, 0);
        controlStart.set(Calendar.MINUTE, 0);
        controlStart.set(Calendar.SECOND, 0);
        controlStart.set(Calendar.MILLISECOND, 0);
        return controlStart;
    }

    public static Calendar GetControlMonth1st(Date Control_Start) {
        Calendar month1st = Calendar.getInstance();
        month1st.setTime(Control_Start);
        month1st.set(Calendar.DAY_OF_MONTH, 1);
        month1st.set(Calendar.HOUR_OF_DAY, 0);
        month1st.set(Calendar.MINUTE, 0);
        month1st.set(Calendar.SECOND, 0);
        month1st.set(Calendar.MILLISECOND, 0);
        return month1st;
    }

    public static Calendar FixTime(Calendar source, int days, int hours, int minutes, int seconds, int millseconds) {
        Calendar result = (Calendar) source.clone();
        result.add(Calendar.DAY_OF_YEAR, days);
        result.add(Calendar.HOUR_OF_DAY, hours);
        result.add(Calendar.MINUTE, minutes);
        result.add(Calendar.SECOND, seconds);
        result.add(Calendar.MILLISECOND, millseconds);
        return result;
    }

    /***
     * ??????L2??????????????????
     *
     * @param input
     * @param device
     * @param service
     * @return
     */
    public static boolean IsL2ConfigActive(PolicyProfile config, PolicyDevice device, PolicyService service) {
        try {
            return config.getItem().getItems()[device.value()][service.value()] == 1;
        } catch (Throwable ex) {
            return false;
        }
    }

    public static Level3 IsL3ConfigActive(PolicyProfile config) {
        try {
            return config.getParam();
        } catch (Throwable ex) {
            Level3 o = new Level3(1, 1);
            return o;
        }
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static Date getHourlyStartTime(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getFirstDate(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getEndOfDate(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    public static Date addMonths(Date origDate, int months) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(origDate);
        cal.add(Calendar.MONTH, months);
        return cal.getTime();
    }

    public static Date addDays(Date origDate, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(origDate);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    public static Date addMinutes(Date origDate, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(origDate);
        cal.add(Calendar.MINUTE, minutes);
        return cal.getTime();
    }

    //???????????????????????????
    public static int countMonth(Date startTime, Date endTime) {
        Calendar start = Calendar.getInstance();
        start.setTime(startTime);
        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        return (end.get(Calendar.MONTH) + 1) + ((end.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12) -
                (start.get(Calendar.MONTH) + 1) + 1;
    }

    /**
     * ??????????????????d1???d2????????????(24??????)<br>
     * ???????????????d2-d1??????????????????24???????????????????????????????????????????????????(d1>d2)?????????(d1==d2)?????????<br>
     *
     * @param d1 ???????????????
     * @param d2 ???????????????
     * @return ????????????(d2 - d1)????????????24??????
     */
    public static int getDatesInterval(Date d1, Date d2) {
        long diff = d2.getTime() - d1.getTime();
        return Math.toIntExact(diff / TimeUnit.DAYS.toMillis(1));
    }

    /**
     * millisecond?????????????????????15????????????(kWh)
     *
     * @param kWh ????????????????????????
     * @param sec ??????
     */
    public static BigDecimal secKWhToKWh(BigDecimal kWh, long millisecond) {
        return kWh.multiply(BigDecimal.valueOf(15 * 60 * 1000)).
                divide(BigDecimal.valueOf(millisecond), 3, RoundingMode.HALF_DOWN);
    }

    /**
     * 15????????????(kWh)?????????15????????????
     *
     * @param kWh ??????
     */
    public static BigDecimal kWhTo15KW(BigDecimal kWh) {
        return kWh.multiply(kwhTokW);
    }

    /**
     * millisecond???????????????????????????15????????????
     *
     * @param kWh         ???????????????????????????
     * @param millisecond
     */
    public static BigDecimal secKWhTo15KW(BigDecimal kWh, long millisecond) {
        return kWhTo15KW(secKWhToKWh(kWh, millisecond));
    }

}