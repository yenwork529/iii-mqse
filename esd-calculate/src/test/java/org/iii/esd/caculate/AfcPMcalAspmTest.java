package org.iii.esd.caculate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.afc.performance.PMcalAspm;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {PMcalAspm.class})
@EnableAutoConfiguration
@Log4j2
public class AfcPMcalAspmTest {

    @Autowired
    private PMcalAspm aspmModule;

    @Test
    public void testPMcal_ASPM() {
        List<BigDecimal> spmList = new ArrayList<BigDecimal>();
        spmList.add(new BigDecimal(50.0));
        spmList.add(new BigDecimal(51.0));
        spmList.add(new BigDecimal(52.0));
        spmList.add(new BigDecimal(50.0));

        aspmModule.setSpmList(spmList);
        BigDecimal aspm = aspmModule.calculate();
        log.info("aspm=" + aspm);

        if (aspm == null) {
            fail();
        }
    }
}
