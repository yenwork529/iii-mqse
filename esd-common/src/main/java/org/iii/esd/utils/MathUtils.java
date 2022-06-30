package org.iii.esd.utils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;

public class MathUtils {

    public static BigDecimal sum(BigDecimal a, BigDecimal b) {
        Optional<BigDecimal> r1 = Optional.ofNullable(a);
        Optional<BigDecimal> r2 = Optional.ofNullable(b);
        if (r1.isPresent()) {
            return r1.get().add(r2.orElse(BigDecimal.ZERO));
        } else {
            return r2.orElse(null);
        }
    }

    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        Optional<BigDecimal> r1 = Optional.ofNullable(a);
        Optional<BigDecimal> r2 = Optional.ofNullable(b);
        if (r1.isPresent() && r2.isPresent()) {
            return r1.get().multiply(r2.get());
        } else {
            return null;
        }
    }

    /**
     * 四捨五入小數
     *
     * @param val
     * @param scale
     */
    public static BigDecimal roundBigDecimal(double val, int scale) {
        return new BigDecimal(val).setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

    public static char random() {
        Random random = new Random();
        int r = random.nextInt(75) + 48;
        return (r > 57 && r < 65 || r > 90 && r < 97 ? random() : (char) r);
    }

    public static String random(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random());
        }
        return sb.toString();
    }

}