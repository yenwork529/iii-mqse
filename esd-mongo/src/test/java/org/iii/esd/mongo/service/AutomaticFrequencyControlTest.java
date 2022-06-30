package org.iii.esd.mongo.service;

import java.util.List;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.mongo.document.SiloCompanyProfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {
        AutomaticFrequencyControlService.class,
        UpdateService.class})
@EnableAutoConfiguration
@Log4j2
class AutomaticFrequencyControlTest extends AbstractServiceTest {

    @Value("${companyId}")
    private Long companyId;

    @Value("${afcId}")
    private Long afcId;

    @Autowired
    private AutomaticFrequencyControlService service;

    @Test
    @Disabled
    void testAddAutomaticFrequencyControlProfile() {
        AutomaticFrequencyControlProfile obj = new AutomaticFrequencyControlProfile();
        obj.setName("III-AFC");
        obj.setSiloCompanyProfile(new SiloCompanyProfile(companyId));
        obj.setEnableStatus(EnableStatus.enable);
        long seq = service.addAutomaticFrequencyControlProfile(obj);
        log.info(seq);
    }

    @Test
    @Disabled
    void testUpdateAutomaticFrequencyControlProfile() {
        Optional<AutomaticFrequencyControlProfile> opt = service.findAutomaticFrequencyControlProfile(afcId);
        if (opt.isPresent()) {
            AutomaticFrequencyControlProfile obj = opt.get();
            String oldName = obj.getName();
            String newName = "III-AFCTest";
            obj.setName(newName);
            service.updateAutomaticFrequencyControlProfile(obj);
            obj = service.findAutomaticFrequencyControlProfile(afcId).get();
            assertEquals(obj.getName(), newName);
            obj.setName(oldName);
            service.updateAutomaticFrequencyControlProfile(obj);
        } else {
            fail();
        }
    }

    @Test
    void testFindAutomaticFrequencyControlProfile() {
        Optional<AutomaticFrequencyControlProfile> opt = service.findAutomaticFrequencyControlProfile(afcId);
        if (opt.isPresent()) {
            log.info(opt.get().toString());
        } else {
            fail();
        }
    }

    @Test
    void testFindAllAutomaticFrequencyControlProfile() {
        List<AutomaticFrequencyControlProfile> list = service.findAllAutomaticFrequencyControlProfile();
        log.info(list.size());
        list.forEach(obj -> log.info(obj.toString()));
    }

    @Test
    void testFindEnableAutomaticFrequencyControlProfile() {
        List<AutomaticFrequencyControlProfile> list = service.findEnableAutomaticFrequencyControlProfile();
        log.info(list.size());
        list.forEach(obj -> log.info(obj.toString()));
    }

    @Test
    @Disabled
    void testDeleteAutomaticFrequencyControlProfile() {
        AutomaticFrequencyControlProfile obj = new AutomaticFrequencyControlProfile();
        obj.setName("TEST");
        long seq = service.addAutomaticFrequencyControlProfile(obj);
        service.deleteAutomaticFrequencyControlProfile(seq);
        Optional<AutomaticFrequencyControlProfile> opt = service.findAutomaticFrequencyControlProfile(seq);
        if (opt.isPresent()) {
            fail();
        }
    }

}