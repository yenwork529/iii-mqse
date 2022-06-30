package org.iii.esd.caculate;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.afc.algorithm.Interpolation;

@SpringBootTest(classes = {Interpolation.class})
@EnableAutoConfiguration
@Log4j2
public class InterpolationTest {

    @Test
    public void testInterpolation() {
        double[] x = {59.50, 59.625, 59.75};
        double[] y = {100, 74, 48};

        double frequency = 59.625;
        double power = Interpolation.calculate(x, y, frequency);

        log.info("f(" + frequency + ") = " + power);
    }
}
