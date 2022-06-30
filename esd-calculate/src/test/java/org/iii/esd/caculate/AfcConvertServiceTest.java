package org.iii.esd.caculate;

import java.util.Arrays;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.afc.service.ConvertService;

import static org.iii.esd.afc.algorithm.DefaultStrategy.SOC_BASELINE;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {ConvertService.class})
@EnableAutoConfiguration
@Log4j2
public class AfcConvertServiceTest {

    @Autowired
    private ConvertService service;

    @Test
    public void testConvertService() {
        //		Double frequency = 59.87;
        Double frequency = 60.14;
        Double soc = 49.99;
        Double powerRatio = service.run(frequency, soc);
        log.info("frequency=" + frequency + "&soc=" + soc + "==>powerRatio=" + powerRatio);

        if (powerRatio == null) {
            fail();
        }
    }

    @Test
    public void testConvertServiceSecondEdition() {
        Double[] frequencies = {59.87, 60.14};
        Double lastPowerRatio = -50.04;
        Double soc = SOC_BASELINE;
        Double powerRatio = service.run(frequencies, lastPowerRatio, soc);
        log.info("frequencies=" + Arrays.toString(frequencies) + "&lastPowerRatio=" + lastPowerRatio + "&soc=" + soc + "==>powerRatio=" +
                powerRatio);

        if (powerRatio == null) {
            fail();
        }
    }

    @Test
    public void testBasicRequirement() {
        Long[] timestamps = {1588242650L, 1588242651L, 1588242652L};
        Double[] frequencies = {60.38, 59.63, 60.50};
        Double[] socs = {49.99, 49.99, 49.99};
        Double[] powerRatios = new Double[timestamps.length];

        // init powerRatio
        for (int i = 0; i < timestamps.length; i++) {
            powerRatios[i] = service.run(frequencies[i], socs[i]);
        }

        // must test in zone which is out of dead band
        for (int j = timestamps.length - 1; j >= 0; j--) {
            int k = j - 1;
            if (k < 0) {
                break;
            }
            Double F = frequencies[j] - frequencies[k];
            log.info("F(" + j + "," + k + ")=" + F);
            Double P = powerRatios[j] - powerRatios[k];
            log.info("P(" + j + "," + k + ")=" + P);
            log.info("F(" + j + "," + k + ")xP(" + j + "," + k + ")=" + (F * P));

            // F & P should be in relationship of inverse ratio, so if (FxP) is less or equals zero, then this test case is correct
            if (F * P > 0) {
                fail();
            }
        }
    }
}
