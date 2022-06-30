package org.iii.esd.mongo.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.enums.TouType;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.PolicyProfile;
import org.iii.esd.mongo.document.SpinReserveProfile;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {FieldProfileService.class, UpdateService.class})
@EnableAutoConfiguration
@Log4j2
class FieldProfileServiceTest extends AbstractServiceTest {

    @Value("${fieldId}")
    private Long id;

    @Value("${policyId}")
    private Long policyId;

    @Value("${afcId}")
    private Long afcId;

    @Value("${drId}")
    private Long drId;

    @Value("${srId}")
    private Long srId;

    @Autowired
    private FieldProfileService service;

    @Test
    @Disabled
    void testAdd() {
        FieldProfile obj = new FieldProfile();
        obj.setName("數位所5F");
        obj.setTouType(TouType.TPH3S);
        long seq = service.add(obj);
        log.info(seq);

        //		FieldProfile obj = new FieldProfile();
        //		obj.setName("亞力測試場域");
        //		obj.setTouType(TouType.TPH3S);
        //		obj.setCompanyProfile(new CompanyProfile(2l));
        //		obj.setTyod(723);
        //		obj.setOyod(768);
        //		obj.setTyodc(723);
        //		obj.setTcEnable(EnableStatus.enable);
        //		long seq = service.add(obj);
        //		log.info(seq);

    }

    @Test
    void testFind() {
        Optional<FieldProfile> opt = service.find(id);
        if (opt.isPresent()) {
            log.info(opt.get().toString());
        } else {
            fail();
        }
    }

    @Test
    void testFindEnableFieldProfile() {
        List<FieldProfile> list = service.findEnableFieldProfile();
        log.info(list.size());
        String l = list.stream().map(f -> f.getId().toString()).collect(Collectors.joining(","));
        log.info(l);
    }

    @Test
    void testFindFieldProfileBySrId() {
        List<FieldProfile> list = service.findFieldProfileBySrId(srId, EnableStatus.enable);
        log.info(list.size());
        String l = list.stream().map(f -> f.getId().toString()).collect(Collectors.joining(","));
        log.info(l);
    }

    @Test
    void testFindFieldProfileBySrIdOrderBySrIndex() {
        List<FieldProfile> list = service.findFieldProfileBySrIdOrderBySrIndex(3l);
        log.info(list.size());
        String l = list.stream().map(f -> f.getId().toString()).collect(Collectors.joining(","));
        log.info(l);
    }

    @Test
    void testFindByExample() {
        List<FieldProfile> list = service.findByExample(null, null, null, null, null);
        log.info(list.size());
        list.forEach(obj -> log.info(obj.toString()));

        int list2 = service.countBySrId(null);
        log.info(list2);
    }


    @Test
    @Disabled
    void testUpdate() {
        Optional<FieldProfile> opt = service.find(id);
        if (opt.isPresent()) {
            FieldProfile obj = opt.get();
            //obj.setName("測試場域Test");
            obj.setTouType(TouType.TPH3S);
            obj.setPolicyProfile(new PolicyProfile(policyId));
            obj.setSpinReserveProfile(new SpinReserveProfile(srId));
            log.info(service.update(obj).toString());
        } else {
            fail();
        }
    }

    @Test
    void testCountBySrId() {
        int count = service.countBySrId(srId);
        log.info(count);
    }

    @Test
    @Disabled
    void testDelete() {
        FieldProfile obj = new FieldProfile();
        obj.setName("測試場域");
        obj.setTouType(TouType.TPMRL2S);
        long seq = service.add(obj);
        service.delete(seq);
        Optional<FieldProfile> opt = service.find(seq);
        if (opt.isPresent()) {
            fail();
        }
    }

}