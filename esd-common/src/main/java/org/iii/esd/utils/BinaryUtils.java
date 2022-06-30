package org.iii.esd.utils;

import java.math.BigDecimal;

public class BinaryUtils {

    /**
     * Calculator Signed Value
     *
     * @param value
     */
    public static BigDecimal calSigned(BigDecimal value) {
        return new BigDecimal(value.shortValue());
    }

    /**
     * Calculator Two Words Value (2^16*high + low)
     *
     * @param high
     * @param low
     */
    public static BigDecimal calTwoWordsValue(int high, int low) {
        return new BigDecimal(2).pow(16).multiply(new BigDecimal(high)).add(new BigDecimal(low));
    }

    /**
     * Calculator ieee754
     *
     * @param value
     */
    public static BigDecimal ieee754(BigDecimal value) {
        return new BigDecimal("" + Float.intBitsToFloat(value.intValue()));
    }

    /**
     * Calculator ieee754
     *
     * @param value
     */
    public static BigDecimal ieee754(int value) {
        return new BigDecimal("" + Float.intBitsToFloat(value));
    }

}