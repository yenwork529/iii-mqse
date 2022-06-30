package org.iii.esd.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 檢核工具
 *
 * @author yuchen
 */
public class ValidationUtils {
    public static final String EMAIL = "^\\w{1,63}@[a-zA-Z0-9]{2,63}\\.[a-zA-Z]{2,63}(\\.[a-zA-Z]{2,63})?$";
    public static final String TIMEZONE = "GMT[+-]([01][0-2]|0\\d):([0-5]\\d)";
    public static final String FILE = "\\\\|\\/|\\||:|\\?|\\*|\"|<|>|\\^|\\p{Cntrl}";
    // 同時包含字母、數字、特殊符號的8位以上組合
    public static final String PW_PATTERN = "^(?![A-Za-z0-9]+$)(?![0-9\\W_]+$)(?![A-Za-z\\W_]+$).{8,}$";

    public static final String ALPHA_NUMERIC = "^[a-zA-Z0-9]*$";

    /**
     * 檢核mail格式是否正確
     *
     * @param email
     */
    public static boolean isEmailValid(String email) {
        return email.matches(EMAIL);
    }

    /**
     * 檢核時區格式是否正確
     *
     * @param timezone
     */
    public static boolean isTimeZoneValid(String timezone) {
        return timezone.matches(TIMEZONE);
    }

    /**
     * 判斷字串是否為HEX編碼
     *
     * @param str
     */
    public static boolean isHex(String str) {
        int strLength = str.length();
        return (strLength > 0) && (strLength % 2 == 0) && str.matches("^[0-9A-Fa-f]+$");
    }

    /**
     * 是否符合英數字
     *
     * @param str
     */
    public static boolean isAlphaNumeric(String str) {
        return isNotBlank(str) && str.matches(ALPHA_NUMERIC);
    }

    /**
     * @param value
     * @param lowerbound
     * @param upperbound
     */
    public static boolean isInRange(int value, int lowerbound, int upperbound) {
        return (value >= lowerbound && value <= upperbound);
    }

    /**
     * 檢核password格式是否合法
     *
     * @param password
     */
    public static boolean isPasswordValid(String password) {
        return password.matches(PW_PATTERN);
    }

    private static boolean isNotBlank(String text) {
        return StringUtils.isNotBlank(text);
    }
}