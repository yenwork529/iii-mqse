package org.iii.esd.mongo.service;

import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.mongo.document.PolicyProfile;
import org.iii.esd.mongo.vo.Level1;
import org.iii.esd.mongo.vo.Level2;
import org.iii.esd.mongo.vo.Level3;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {PolicyProfileService.class, UpdateService.class})
@EnableAutoConfiguration
@Log4j2
class PolicyProfileServiceTest extends AbstractServiceTest {

    @Value("${policyId}")
    private Long id;

    @Autowired
    private PolicyProfileService service;

    @Test
    @Disabled
    void testAdd() {
        PolicyProfile obj = new PolicyProfile();
        obj.setName("聚合調度");
        obj.setDispatching(new Level1(1, 1));
        obj.setItem(new Level2(new int[][]{{1, 1, 1, 1, 1}, {1, 1, 1, 1, 1}, {1, 1, 1, 1, 1}, {1, 1, 1, 1, 1}, {1, 1, 1, 1, 1}}));
        obj.setParam(new Level3(1, 1));
        long seq = service.add(obj);
        log.info(seq);
    }

    @Test
    void testFind() {
        Optional<PolicyProfile> opt = service.find(id);
        if (opt.isPresent()) {
            log.info(opt.get().toString());
        } else {
            fail();
        }
    }

    @Test
    @Disabled
    void testUpdate() {
        Optional<PolicyProfile> opt = service.find(id);
        if (opt.isPresent()) {
            PolicyProfile obj = opt.get();
            obj.setName("聚合調度2");
            log.info(service.update(obj).toString());
        } else {
            fail();
        }
    }

    @Test
    @Disabled
    void testDelete() {
        PolicyProfile obj = new PolicyProfile();
        obj.setName("聚合調度");
        long seq = service.add(obj);
        service.delete(seq);
        Optional<PolicyProfile> opt = service.find(seq);
        if (opt.isPresent()) {
            fail();
        }
    }
}
