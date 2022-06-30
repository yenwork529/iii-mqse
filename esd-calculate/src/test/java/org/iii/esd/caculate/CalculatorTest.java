package org.iii.esd.caculate;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import org.iii.esd.afc.utils.Calculator;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
public class CalculatorTest {

    @Test
    public void testPercentageLimit() {
        Double v1 = Calculator.getPercentageLimit(90.0);
        log.info("v1=" + v1);
        assertTrue(v1.equals(90.0));

        Double v2 = Calculator.getPercentageLimit(101.0);
        log.info("v2=" + v2);
        assertTrue(v2.equals(100.0));

        Double v3 = Calculator.getPercentageLimit(0.0);
        log.info("v3=" + v3);
        assertTrue(v3.equals(0.0));

        Double v4 = Calculator.getPercentageLimit(-10.0);
        log.info("v4=" + v4);
        assertTrue(v4.equals(-10.0));

        Double v5 = Calculator.getPercentageLimit(-110.0);
        log.info("v5=" + v5);
        assertTrue(v5.equals(-100.0));
    }

    @Test
    public void testFrequencyLimit() {
        Double v1 = Calculator.getFrequencyLimit(60.60);
        log.info("v1=" + v1);
        assertTrue(v1.equals(60.50));

        Double v2 = Calculator.getFrequencyLimit(60.50);
        log.info("v2=" + v2);
        assertTrue(v2.equals(60.50));

        Double v3 = Calculator.getFrequencyLimit(60.40);
        log.info("v3=" + v3);
        assertTrue(v3.equals(60.40));

        Double v4 = Calculator.getFrequencyLimit(59.60);
        log.info("v4=" + v4);
        assertTrue(v4.equals(59.60));

        Double v5 = Calculator.getFrequencyLimit(59.50);
        log.info("v5=" + v5);
        assertTrue(v5.equals(59.50));

        Double v6 = Calculator.getFrequencyLimit(59.40);
        log.info("v6=" + v6);
        assertTrue(v6.equals(59.50));

        Double v7 = Calculator.getFrequencyLimit(60.0);
        log.info("v7=" + v7);
        assertTrue(v7.equals(60.0));
    }
}
