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

import org.iii.esd.mongo.document.DemandResponseProfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {
        DemandResponseService.class,
        UpdateService.class})
@EnableAutoConfiguration
@Log4j2
class DemandResponseServiceTest extends AbstractServiceTest {

    @Value("${companyId}")
    private Long companyId;

    @Value("${drId}")
    private Long drId;

    @Autowired
    private DemandResponseService service;

    @Test
    @Disabled
    void testAddDemandResponseProfile() {
        DemandResponseProfile obj = new DemandResponseProfile();
        obj.setName("III-DR");
        long seq = service.addDemandResponseProfile(obj);
        log.info(seq);
    }

    @Test
    @Disabled
    void testUpdateDemandResponseProfile() {
        Optional<DemandResponseProfile> opt = service.findDemandResponseProfile(drId);
        if (opt.isPresent()) {
            DemandResponseProfile obj = opt.get();
            String oldName = obj.getName();
            String newName = "III-DRTest";
            obj.setName(newName);
            service.updateDemandResponseProfile(obj);
            obj = service.findDemandResponseProfile(drId).get();
            assertEquals(obj.getName(), newName);
            obj.setName(oldName);
            service.updateDemandResponseProfile(obj);
        } else {
            fail();
        }
    }

    @Test
    void testFindDemandResponseProfile() {
        Optional<DemandResponseProfile> opt = service.findDemandResponseProfile(drId);
        if (opt.isPresent()) {
            log.info(opt.get().toString());
        } else {
            fail();
        }
    }

    @Test
    void testFindAllDemandResponseProfile() {
        List<DemandResponseProfile> list = service.findAllDemandResponseProfile();
        log.info(list.size());
        list.forEach(obj -> log.info(obj.toString()));
    }

    @Test
    void testFindEnableDemandResponseProfile() {
        List<DemandResponseProfile> list = service.findEnableDemandResponseProfile();
        log.info(list.size());
        list.forEach(obj -> log.info(obj.toString()));
    }

    @Test
    @Disabled
    void testDeleteDemandResponseProfile() {
        DemandResponseProfile obj = new DemandResponseProfile();
        obj.setName("TEST");
        long seq = service.addDemandResponseProfile(obj);
        service.deleteDemandResponseProfile(seq);
        Optional<DemandResponseProfile> opt = service.findDemandResponseProfile(seq);
        if (opt.isPresent()) {
            fail();
        }
    }

}