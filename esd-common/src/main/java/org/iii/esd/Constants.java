package org.iii.esd;

import java.text.SimpleDateFormat;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class Constants {

    public static final int MODE_STANDBY = 0;
    public static final int MODE_CONNECTTION = 2;
    public static final int MODE_MANNAUL = 0;

    public static final int CHARGE_MIN_PERCENT = 0;
    public static final int CHARGE_MAX_PERCENT = 100;

    public static final int CONTROL_STOP = 0;
    public static final int CONTROL_START = 1;

    public static final String PERIOD = ".";

    public static final SimpleDateFormat TIMESTAMP_SHORT_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    public static final SimpleDateFormat DATETIME_SHORT_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");
    public static final SimpleDateFormat DATEHOUR_SHORT_FORMAT = new SimpleDateFormat("yyyyMMddHH");
    public static final SimpleDateFormat DATE_SHORT_FORMAT = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat MONTH_SHORT_FORMAT = new SimpleDateFormat("yyyyMM");
    public static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat TIMESTAMP_FORMAT2 = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    public static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final SimpleDateFormat DATETIME_ZERO_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:00");
    public static final SimpleDateFormat DATEHOUR_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH");
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat DATE_FORMAT2 = new SimpleDateFormat("yyyy/MM/dd");
    public static final SimpleDateFormat DATE_FORMAT3 = new SimpleDateFormat("dd/MM");
    public static final SimpleDateFormat DATE_FORMAT4 = new SimpleDateFormat("M/d");
    public static final SimpleDateFormat DATE01_FORMAT = new SimpleDateFormat("yyyy-MM-01");
    public static final SimpleDateFormat YEAR_MONTH_FORMAT = new SimpleDateFormat("yyyy-MM");
    public static final SimpleDateFormat YEAR_MONTH_FORMAT2 = new SimpleDateFormat("yyyy/MM");
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");
    public static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MM");
    public static final SimpleDateFormat DAYOFMONTH_FORMAT = new SimpleDateFormat("dd");
    public static final SimpleDateFormat HOUROFDAY_FORMAT = new SimpleDateFormat("HH");
    public static final SimpleDateFormat MINUTE_FORMAT = new SimpleDateFormat("mm");
    public static final SimpleDateFormat SECOND_FORMAT = new SimpleDateFormat("ss");
    public static final SimpleDateFormat ISO8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static final SimpleDateFormat ISO8601_FORMAT2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static final SimpleDateFormat ISO8601_FORMAT3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 數字格式:小數點後0位
     */
    public static final String DECIMAL_PLACE_0 = "#";
    /**
     * 數字格式:小數點後0位, 加千分位
     */
    public static final String DECIMAL_PLACE_0_Separator = "#,##0";
    /**
     * 數字格式:小數點後1位
     */
    public static final String DECIMAL_PLACE_1 = "#.#";
    /**
     * 數字格式:小數點後1位 沒有的話補0
     */
    public static final String DECIMAL_PLACE_1_0 = "0.0";
    /**
     * 數字格式:小數點後1位 沒有的話補0, 加千分位
     */
    public static final String DECIMAL_PLACE_1_0_Separator = "#,##0.0";
    /**
     * 數字格式:小數點後2位 沒有的話補0
     */
    public static final String DECIMAL_PLACE_2_0 = "0.00";
    /**
     * 數字格式:小數點後3位 沒有的話補0
     */
    public static final String DECIMAL_PLACE_3_0 = "0.000";
    /**
     * 數字格式:小數點後2位
     */
    public static final String DECIMAL_PLACE_2 = "#.##";
    /**
     * 數字格式:小數點後2位, 加千分位
     */
    public static final String DECIMAL_PLACE_2_Separator = "#,##0.##";
    /**
     * 數字格式:小數點後3位
     */
    public static final String DECIMAL_PLACE_3 = "#.###";
    /**
     * 數字格式:小數點後4位
     */
    public static final String DECIMAL_PLACE_4 = "#.####";

    public static final String JSESSIONID = "JSESSIONID";

    public static final String ROLE_DEMO = "0";

    public static final String ROLE_SYSADMIN = "1";

    public static final String ROLE_SIADMIN = "2";
    public static final String ROLE_SIUSER = "3";

    public static final String ROLE_FIELDADMIN = "4";
    public static final String ROLE_FIELDUSER = "5";
    public static final String ROLE_QSEADMIN = "6";
    public static final String ROLE_QSEUSER = "7";

    public static final String ROLE_AFCADMIN = "8";
    public static final String ROLE_AFCUSER = "9";

    public static final String ROLE_QSE_AFC_ADMIN = "10";
    public static final String ROLE_QSE_AFC_USER = "11";

    public static final Set<Long> SYS_AUTHOR = ImmutableSet.of(
            Long.parseLong(ROLE_SYSADMIN));

    public static final Set<Long> QSE_AUTHOR = ImmutableSet.of(
            Long.parseLong(ROLE_QSEADMIN),
            Long.parseLong(ROLE_QSEUSER));

    public static final Set<Long> TXG_AUTHOR = ImmutableSet.of(
            Long.parseLong(ROLE_SIADMIN),
            Long.parseLong(ROLE_SIUSER));

    public static final Set<Long> RES_AUTHOR = ImmutableSet.of(
            Long.parseLong(ROLE_FIELDADMIN),
            Long.parseLong(ROLE_FIELDUSER));

    public static final String CONTENT_TYPE_CSV = "text/csv; charset=UTF-8";
    public static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

    public static final int BIDDING_VALUE_SCALE = 3;
}