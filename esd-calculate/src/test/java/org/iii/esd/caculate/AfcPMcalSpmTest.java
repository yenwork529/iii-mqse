package org.iii.esd.caculate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.afc.performance.PMcalSpm;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {PMcalSpm.class})
@EnableAutoConfiguration
@Log4j2
public class AfcPMcalSpmTest {

    @Autowired
    private PMcalSpm spmModule;

    @Test
    public void testPMcal_SPM() {
        List<BigDecimal> sbspmList = new ArrayList<BigDecimal>();
        sbspmList.add(new BigDecimal(91.0));
        sbspmList.add(new BigDecimal(90.0));
        sbspmList.add(new BigDecimal(89.0));

        spmModule.setSbspmList(sbspmList);
        BigDecimal spm = spmModule.calculate();
        log.info("spm=" + spm);

        if (spm == null) {
            fail();
        }
    }
}
