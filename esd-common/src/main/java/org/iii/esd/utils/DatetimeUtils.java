package org.iii.esd.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import org.iii.esd.Constants;

@Log4j
public class DatetimeUtils {

    public static final String ZONE_ID = "UTC+8";
    public static final int ZONE_OFFSET = 8;

    public static final String[] lunarHoliday = {
            "1/1",
            "1/2",
            "1/3",
            "1/4",
            "1/5",
            "5/5",
            "8/15",
    };

    public static final String[] gregorianHoliday = {
            "1/1",
            "2/28",
            "4/4",
            "4/5",
            "5/1",
            "10/10",
    };
    public static final Instant DEFAULT_EPOCH = Instant.ofEpochMilli(0);
    public static final LocalDateTime ETERNAL = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
    public static final Instant DEFAULT_ETERNAL = toInstant(ETERNAL);

    public static Date min(Date start, Date end) {
        return start.getTime() <= end.getTime() ? start : end;
    }

    public static Date max(Date start, Date end) {
        return start.getTime() >= end.getTime() ? start : end;
    }

    public enum DateTimeEdge {
        EPOCH, ETERNAL;
    }

    public static LocalDateTime parseDateTimeOrDefault(String dateTime, DateTimeFormatter formatter, DateTimeEdge defautEdge) {
        if (StringUtils.isEmpty(dateTime)) {
            switch (defautEdge) {
                case ETERNAL:
                    return toLocalDateTime(DEFAULT_ETERNAL);
                case EPOCH:
                default:
                    return toLocalDateTime(DEFAULT_EPOCH);
            }
        } else {
            return LocalDateTime.parse(dateTime, formatter);
        }
    }

    /**
     * 日期格式轉換
     *
     * @param dateformat
     */
    public static Date parseDate(String dateformat) {
        try {
            return DateUtils.parseDate(dateformat,
                    new String[]{
                            Constants.DATETIME_SHORT_FORMAT.toPattern(),
                            Constants.DATEHOUR_SHORT_FORMAT.toPattern(),
                            Constants.DATE_SHORT_FORMAT.toPattern(),
                            Constants.MONTH_SHORT_FORMAT.toPattern(),
                            Constants.YEAR_FORMAT.toPattern()
                    });
        } catch (ParseException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 日期格式轉換
     *
     * @param date
     * @param dateformat
     */
    public static Date parseDate(String date, SimpleDateFormat dateformat) {
        try {
            return dateformat.parse(date);
        } catch (ParseException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 依據calendarField截斷日期
     *
     * @param date
     * @param calendarField the field from {@code Calendar} ex：Calendar.DATE
     */
    public static Date truncated(Date date, int calendarField) {
        return DateUtils.truncate(date, calendarField);
    }

    /**
     * 取得某天起始00：00
     *
     * @param date
     */
    public static Date getFirstHourOfDay(Date date) {
        return truncated(date, Calendar.DATE);
    }

    /**
     * 取得某天起始23：59 : 59
     *
     * @param date
     */
    public static Date getLastTimeOfDay(Date date) {
        return new Date(getFirstHourOfDay(date).getTime() + ((60 * 60 * 24 - 1) * 1000));
    }

    /**
     * 依據 calendarField 和 amount 換算新的日期
     *
     * @param date
     * @param calendarField
     * @param amount
     */
    public static Date add(Date date, int calendarField, int amount) {
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return c.getTime();
    }

    /**
     * 是否為mins內即時資料
     *
     * @param reportTime
     * @param mins
     */
    public static boolean isRealtimeData(Date reportTime, int mins) {
        return (new Date().getTime() - reportTime.getTime()) < mins * 60 * 1000;
    }

    /**
     * 是否為假日
     *
     * @param date
     */
    public static boolean isHoliday(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || isNationalHoliday(date);
    }

    /**
     * 是否為假日
     *
     * @param calendar
     */
    public static boolean isHoliday(Calendar calendar) {
        return isHoliday(calendar.getTime());
    }

    /**
     * 判斷是否為農曆假日<br> 春節：農曆除夕~1月5日<br> 端午節：農曆5月5日<br> 中秋節：農曆8月15日<br>
     *
     * @param date
     */
    public static boolean isLunarHoliday(Date date) {
        Lunar lunar = LunarUtils.date2Lunar(date).get();
        int year = lunar.getYear();
        int month = lunar.getMonth();
        int day = lunar.getDay();
        return LunarUtils.date2Lunar(add(date, Calendar.DATE, 1)).get().getYear() == year + 1 // 除夕
                || Arrays.asList(lunarHoliday).contains(month + "/" + day);
    }

    /**
     * 判斷是否為國定假日<br> 放假標準是以台電電價表來判斷<br> https://www.taipower.com.tw/upload/238/2018070210412196443.pdf<br> 離峰日如下表所列日期。<br>
     * 中華民國開國紀念日：1月1日<br> 春節：農曆除夕~1月5日<br> 和平紀念日：2月28日<br> 兒童節：4月4日<br> 民族掃墓節：4月4日或4月5日<br> 端午節：農曆5月5日<br> 勞動節：5月 1日<br> 中秋節：農曆8月15日<br>
     * 國慶日：10月10日<br>
     *
     * @param date
     */
    public static boolean isNationalHoliday(Date date) {
        return isLunarHoliday(date) ||
                Arrays.asList(gregorianHoliday).contains(Constants.DATE_FORMAT4.format(date));
    }

    public static Date truncatedToQuarter(Long timestamp) {
        LocalDateTime localDateTime = toLocalDateTime(timestamp);
        LocalDateTime lastQuarter = localDateTime.truncatedTo(ChronoUnit.HOURS).plusMinutes(15 * (localDateTime.getMinute() / 15));
        return Date.from(lastQuarter.atZone(ZoneId.of(ZONE_ID)).toInstant());
    }

    public static String truncatedToQuarter(LocalDateTime time) {
        LocalDateTime lastQuarter = time.truncatedTo(ChronoUnit.HOURS).plusMinutes(15 * (time.getMinute() / 15));
        return lastQuarter.toString();
    }

    public static LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.of(ZONE_ID));
    }

    public static LocalDateTime toLocalDateTime(Date time) {
        return LocalDateTime.ofInstant(time.toInstant(), ZoneId.of(ZONE_ID));
    }

    public static LocalDateTime toLocalDateTime(Long timestamp) {
        return toLocalDateTime(new Date(timestamp));
    }

    public static LocalDateTime toLocalDateTime(String time) {
        LocalDateTime localDateTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        try {
            localDateTime = toLocalDateTime(sdf.parse(time));
        } catch (ParseException ex) {
            log.error(ex.getMessage());
        }
        return localDateTime;
    }

    public static Date toDate(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(ZoneId.of(ZONE_ID)).toInstant();
        return toDate(instant);
    }

    public static Date toDate(Instant instant) {
        return Date.from(instant);
    }

    public static boolean isBetween(Date target, Date start, Date end) {
        return !Objects.isNull(target)
                && 0 <= target.compareTo(start)
                && target.compareTo(end) <= 0;
    }

    public static Instant toInstant(Date date) {
        return date.toInstant();
    }

    public static LocalDate toLocalDate(Date date) {
        return date.toInstant()
                   .atZone(ZoneId.of(ZONE_ID))
                   .toLocalDate();
    }

    public static Date now() {
        return new Date();
    }

    public static Long toEpochMilli(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.ofHours(ZONE_OFFSET)).toEpochMilli();
    }

    public static TypedPair<LocalDateTime> getStartAndEndOfDate(LocalDate date) {
        LocalDateTime startTime = date.atTime(0, 0, 0);
        LocalDateTime endTime = date.atTime(23, 59, 59);

        return TypedPair.cons(startTime, endTime);
    }

    /**
     * 取得現在時間，但不包含秒
     */
    public static Date getNowWithoutSec() {
        return truncated(new Date(), Calendar.MINUTE);
    }

    /**
     * 是否為每小時最後一刻
     */
    public static boolean isLastQuarterOfHour() {
        Calendar c = Calendar.getInstance();
        int minute = c.get(Calendar.MINUTE);
        // return minute >= 45 && minute <= 59;
        return 45 <= minute;
    }

    /**
     * 是否為調度時間(8:00~17:00)
     */
    public static boolean isDispatchHours() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        return hour >= 8 && hour <= 16;
    }

    public static TypedPair<Date> getTimePeriodBeforeNow(int period) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(DatetimeUtils.ZONE_ID));
        LocalDateTime end = now.truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime start = end.minusMinutes(period);

        return TypedPair.cons(toDate(start), toDate(end));
    }

    public static TypedPair<Date> getTimePeriodOfYear(int year) {
        LocalDateTime start = LocalDateTime.of(year, Month.JANUARY, 1, 0, 0, 0, 0);
        LocalDateTime yestoday = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.of(year, yestoday.getMonth(), yestoday.getDayOfMonth(), 23, 59, 59, 999999999);

        return TypedPair.cons(toDate(start), toDate(end));
    }

    public static String toISOFormat(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(date);
    }

    public static long diffSeconds(Instant t1, Instant t2) {
        long diff = t1.getEpochSecond() - t2.getEpochSecond();
        return Math.abs(diff);
    }

    public static long diffMinutes(Instant t1, Instant t2) {
        long seconds = diffSeconds(t1, t2);
        return seconds / 60;
    }

    public static Date getStart(Date date) {
        LocalDateTime dt = toLocalDateTime(date);

        return Date.from(dt.truncatedTo(ChronoUnit.DAYS)
                           .toInstant(ZoneOffset.ofHours(ZONE_OFFSET)));
    }

    public static Date getEnd(Date date) {
        LocalDateTime dt = toLocalDateTime(date);

        return Date.from(dt.plusDays(1)
                           .truncatedTo(ChronoUnit.DAYS)
                           .minusSeconds(1)
                           .toInstant(ZoneOffset.ofHours(ZONE_OFFSET)));
    }

    public static Instant getTomorrowStart() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        LocalDateTime tomorrowStart = tomorrow.truncatedTo(ChronoUnit.DAYS);

        return tomorrowStart.toInstant(ZoneOffset.ofHours(ZONE_OFFSET));
    }

    public static Instant getTodayStart() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.truncatedTo(ChronoUnit.DAYS);

        return todayStart.toInstant(ZoneOffset.ofHours(ZONE_OFFSET));
    }

    public static Instant getTomorrowEnd() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime afterTomorrow = now.plusDays(2);
        LocalDateTime tomorrowEnd = afterTomorrow.truncatedTo(ChronoUnit.DAYS)
                                                 .minusSeconds(1);

        return tomorrowEnd.toInstant(ZoneOffset.ofHours(ZONE_OFFSET));
    }

    public static Instant getStartOfLocal(Instant instant) {
        LocalDateTime local = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        LocalDateTime localStart = local.truncatedTo(ChronoUnit.DAYS);

        return localStart.toInstant(ZoneOffset.ofHours(ZONE_OFFSET));
    }

    public static Instant getEndOfLocal(Instant instant) {
        LocalDateTime local = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        LocalDateTime nextDay = local.plusDays(1);
        LocalDateTime localEnd = nextDay.truncatedTo(ChronoUnit.DAYS)
                                        .minusSeconds(1);

        return localEnd.toInstant(ZoneOffset.ofHours(ZONE_OFFSET));
    }

    public static Instant instantFromLocalDateTime(LocalDateTime local) {
        return local.toInstant(ZoneOffset.ofHours(ZONE_OFFSET));
    }

    public static Date dateFromLocalDateTime(LocalDateTime local) {
        return Date.from(local.toInstant(ZoneOffset.ofHours(ZONE_OFFSET)));
    }

    public static LocalDateTime localFromDate(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.of(ZONE_ID));
    }

    public static Instant toInstant(LocalDateTime local) {
        return local.toInstant(ZoneOffset.ofHours(ZONE_OFFSET));
    }

    public static Date plusOneDay(Date start) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        calendar.add(Calendar.DATE, 1);

        return calendar.getTime();
    }
}