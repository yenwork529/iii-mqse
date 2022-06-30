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

import org.iii.esd.mongo.document.AutomaticFrequencyControlLog;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {
        AutomaticFrequencyControlLogService.class,
        AutomaticFrequencyControlService.class,
        UpdateService.class
})
@EnableAutoConfiguration
@Log4j2
public class AutomaticFrequencyControlLogServiceTest extends AbstractServiceTest {

    private static final Long profileId = 1L;

    private static final Date timestamp1 = new Date(1582819200000L);     //Feb. 28, 2020 00:00:00.000
    private static final Date timestamp2 = new Date(1582905600000L);     //Feb. 29, 2020 00:00:00.000
    private static final Date timestamp3 = new Date(1582905601000L);     //Feb. 29, 2020 00:00:01.000
    private static final Date timestamp4 = new Date(1582905602000L);     //Feb. 29, 2020 00:00:02.000

    @Autowired
    private AutomaticFrequencyControlService automaticFrequencyControlService;

    @Autowired
    private AutomaticFrequencyControlLogService service;

    @Test
    void testAddOrUpdateAll() {
        Optional<AutomaticFrequencyControlProfile> profile =
                automaticFrequencyControlService.findAutomaticFrequencyControlProfile(profileId);
        if (!profile.isPresent()) {
            fail();
        }

        try {
            List<AutomaticFrequencyControlLog> logList = new ArrayList<AutomaticFrequencyControlLog>();

            AutomaticFrequencyControlLog afcLog1 =
                    getLog(profile.get(), timestamp1, new Double(59.50), new Double(50), new Double(100.0), new Double(100.0));
            logList.add(afcLog1);

            AutomaticFrequencyControlLog afcLog2 =
                    getLog(profile.get(), timestamp2, new Double(60.02), new Double(2.25), new Double(4.5), new Double(100.0));
            logList.add(afcLog2);

            AutomaticFrequencyControlLog afcLog3 =
                    getLog(profile.get(), timestamp3, new Double(60.25), new Double(-23.95), new Double(-47.90), new Double(100.0));
            logList.add(afcLog3);

            AutomaticFrequencyControlLog afcLog4 =
                    getLog(profile.get(), timestamp4, new Double(60.0), new Double(0.0), new Double(0.0), new Double(100.0));
            logList.add(afcLog4);

            service.addOrUpdateAll(profile.get().getId(), logList);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            fail();
        }
    }

    @Test
    void testFindAll() {
        List<AutomaticFrequencyControlLog> logList = service.findAll();
        if (logList.size() > 0) {
            log.info("size=" + logList.size());
            log.info(logList);
        } else {
            fail();
        }
    }

    @Test
    void testFindAllByIdAndTime() {
        List<AutomaticFrequencyControlLog> logList = service.findAllByAfcIdAndTime(profileId, timestamp2);
        if (logList.size() > 0) {
            log.info("size=" + logList.size());
            log.info(logList);
        } else {
            fail();
        }
    }

    @Test
    void testFindAllByIdAndTimeRange() {
        List<AutomaticFrequencyControlLog> logList = service.findAllByAfcIdAndTimeRange(profileId, timestamp2, timestamp4);
        if (logList.size() > 0) {
            log.info("size=" + logList.size());
            log.info(logList);
        } else {
            fail();
        }
    }

    @Test
    void testFindOneByIdAndTime() {
        Optional<AutomaticFrequencyControlLog> afcLog = service.findOneByAfcIdAndTime(profileId, timestamp1);
        if (!afcLog.isPresent()) {
            fail();
        }
        log.info(afcLog.get());
    }

    private AutomaticFrequencyControlLog getLog(
            AutomaticFrequencyControlProfile automaticFrequencyControlProfile, Date timestamp, Double frequency,
            Double ess_power, Double ess_power_ratio, Double sbspm) {
        AutomaticFrequencyControlLog afcLog = new AutomaticFrequencyControlLog();
        afcLog.setAutomaticFrequencyControlProfile(automaticFrequencyControlProfile);
        afcLog.setTimestamp(timestamp);
        afcLog.setFrequency(new BigDecimal(frequency));
        afcLog.setEssPower(new BigDecimal(ess_power));
        afcLog.setEssPowerRatio(new BigDecimal(ess_power_ratio));
        afcLog.setSbspm(new BigDecimal(sbspm));
        return afcLog;
    }
}
