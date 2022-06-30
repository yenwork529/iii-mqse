package org.iii.esd.afc.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.List;

import lombok.extern.log4j.Log4j2;

import org.iii.esd.afc.def.FrequencyEnum;
import org.iii.esd.afc.def.PowerRatioEnum;

@Log4j2
public class Calculator {

    public static Double getHalfAmount(Double original) {
        return original / 2;
    }

    public static Double getAverage(FrequencyEnum f1, FrequencyEnum f2) {
        return getHalfAmount(f1.getFrequency() + f2.getFrequency());
    }

    public static Double getAverage(PowerRatioEnum p1, PowerRatioEnum p2) {
        return getHalfAmount(p1.getPowerRatio() + p2.getPowerRatio());
    }

    public static Double getFractionDigits(Double orig, int digits) {
        Double after = new BigDecimal(orig).setScale(digits, BigDecimal.ROUND_HALF_UP).doubleValue();
        return after;
    }

    public static BigDecimal getAvgOfList(List<BigDecimal> values) {
        if (values == null || values.size() == 0) {
            return null;
        }

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal average = BigDecimal.ZERO;
        Iterator<BigDecimal> iterator = values.iterator();
        try {
            while (iterator.hasNext()) {
                BigDecimal value = iterator.next();
                if (value != null) {
                    total = total.add(value);
                }
            }
            average = total.divide(new BigDecimal(values.size()), 2, RoundingMode.HALF_UP);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return average;
    }

    public static boolean isInverseRatioRelationship(Double[] frequencies, Double[] actualPowerRatios) {
        Double F = frequencies[1] - frequencies[0];
        Double P = actualPowerRatios[1] - actualPowerRatios[0];
        Double FxP = F * P;

        if (FxP <= 0) { return true; } else { return false; }
    }

    public static Double getPercentageLimit(Double input) {
        if (input > 0) {
            return Math.min(input, 100.0);
        } else if (input < 0) {
            return Math.max(input, -100.0);
        }
        return input;
    }

    public static Double getFrequencyLimit(Double input) {
        if (input > FrequencyEnum.F.getFrequency()) {
            return FrequencyEnum.F.getFrequency();
        } else if (input < FrequencyEnum.A.getFrequency()) {
            return FrequencyEnum.A.getFrequency();
        }
        return input;
    }
}
