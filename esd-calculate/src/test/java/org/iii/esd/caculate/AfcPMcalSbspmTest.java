package org.iii.esd.caculate;

import java.math.BigDecimal;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.afc.performance.PMcalSbspm;
import org.iii.esd.afc.service.ConvertService;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {PMcalSbspm.class, ConvertService.class})
@EnableAutoConfiguration
@Log4j2
public class AfcPMcalSbspmTest {

    @Autowired
    private PMcalSbspm sbspmModule;

    @Test
    public void testPMcal_SBSPM() {
        Double[] frequencies = new Double[]{59.87, 60.14};
        //		Double[] actualPowerRatios = new Double[] {22.96, -25.04};
        //		Double[] actualPowerRatios = new Double[] {-26.04, -26.04};
        Double[] actualPowerRatios = new Double[]{-50.04, -50.04};

        sbspmModule.setFrequencies(frequencies);
        sbspmModule.setActualPowerRatios(actualPowerRatios);
        BigDecimal sbspm = sbspmModule.calculate();
        //		log.info("frequencies="+frequencies+"&actualPowerRatios="+actualPowerRatios+"==>sbspm="+sbspm);
        log.info("sbspm=" + sbspm);

        if (sbspm == null) {
            fail();
        }
    }
}
