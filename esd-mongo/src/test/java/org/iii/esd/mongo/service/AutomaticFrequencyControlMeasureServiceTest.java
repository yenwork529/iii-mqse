package org.iii.esd.mongo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.mongo.document.AutomaticFrequencyControlMeasure;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;

import static org.iii.esd.mongo.enums.MeasureType.SPM;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {
        AutomaticFrequencyControlMeasureService.class,
        AutomaticFrequencyControlService.class,
        UpdateService.class
})
@EnableAutoConfiguration
@Log4j2
public class AutomaticFrequencyControlMeasureServiceTest extends AbstractServiceTest {

    private static final Long profileId = 1L;

    private static final Date timestamp = new Date(1582905600000L);     //Feb. 29, 2020 00:00:00.000

    @Autowired
    private AutomaticFrequencyControlService automaticFrequencyControlService;

    @Autowired
    private AutomaticFrequencyControlMeasureService service;

    @Test
    void testAddOrUpdateAll() {
        Optional<AutomaticFrequencyControlProfile> profile =
                automaticFrequencyControlService.findAutomaticFrequencyControlProfile(profileId);
        if (!profile.isPresent()) {
            fail();
        }

        try {
            List<AutomaticFrequencyControlMeasure> measureList = new ArrayList<AutomaticFrequencyControlMeasure>();

            AutomaticFrequencyControlMeasure afcMeasure1 = getMeasure(profile.get(), timestamp, SPM.getShortName(), new Double(87.0), 900);
            measureList.add(afcMeasure1);

            service.addOrUpdateAll(profile.get().getId(), measureList);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            fail();
        }
    }

    private AutomaticFrequencyControlMeasure getMeasure(
            AutomaticFrequencyControlProfile automaticFrequencyControlProfile, Date timestamp, String type,
            Double value, Integer count) {
        AutomaticFrequencyControlMeasure measure = new AutomaticFrequencyControlMeasure();
        measure.setAutomaticFrequencyControlProfile(automaticFrequencyControlProfile);
        measure.setTimestamp(timestamp);
        measure.setType(type);
        measure.setValue(new BigDecimal(value));
        measure.setCount(count);
        return measure;
    }
}
