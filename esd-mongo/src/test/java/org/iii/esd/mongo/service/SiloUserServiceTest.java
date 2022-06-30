package org.iii.esd.mongo.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.SiloUserProfile;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {SiloUserService.class, UpdateService.class})
@EnableAutoConfiguration
@Log4j2
class SiloUserServiceTest extends AbstractServiceTest {

    @Value("${userId}")
    private Long id;

    @Value("${email}")
    private String email;

    @Value("${fieldId}")
    private Long fieldId;

    @Value("${companyId}")
    private Long companyId;

    @Value("${afcId}")
    private Long afcId;

    @Value("${drId}")
    private Long drId;

    @Value("${srId}")
    private Long srId;

    @Autowired
    private SiloUserService service;

    @Test
    void testAdd() {
        SiloUserProfile obj = new SiloUserProfile();
        obj.setEmail("iiisimulate@gmail.com");
        obj.setName("數位所5F管理者");
        //		obj.setCompanyProfile(new CompanyProfile(companyId));
        //		obj.setFieldProfile(new FieldProfile(3l));
        obj.setPassword("dtiisno1!");
        obj.setRoleIds(new HashSet<>(Arrays.asList(4l)));
        obj.setEnableStatus(EnableStatus.enable);
        //		obj.setEmail("ooo@xxx.com");
        //		obj.setName("測試帳號");
        //		obj.setCompanyProfile(new CompanyProfile(companyId));
        //		obj.setPassword("abc123");
        //		obj.setEnableStatus(EnableStatus.enable);

        long seq = service.add(obj);
        log.info(seq);
    }

    @Test
    void testUpdate() {
        Optional<SiloUserProfile> opt = service.find(id);
        if (opt.isPresent()) {
            SiloUserProfile obj = opt.get();
            obj.setName("Test");
            log.info(service.update(obj).toString());
        } else {
            fail();
        }
    }

    @Test
    void testUpdatePasswordRetryAttempts() {
        log.info(service.updatePasswordRetryAttempts(email));
    }

    @Test
    void testResetPasswordRetry() {
        log.info(service.resetPasswordRetry(email));
    }

    @Test
    void testCheckPasswordRetryAttempts() {
        log.info(service.checkPasswordRetryAttempts(email, 5, 100));
    }

    @Test
    void testFindById() {
        Optional<SiloUserProfile> opt = service.find(id);
        if (opt.isPresent()) {
            log.info(opt.get().toString());
        } else {
            fail();
        }
    }

    @Test
    void testFindByEmail() {
        SiloUserProfile obj = service.findByEmail(email);
        log.info(obj.toString());
    }

    @Test
    void testFindAll() {
        List<SiloUserProfile> list = service.findAll();
        log.info(list.size());
        list.forEach(user -> log.info(user.toString()));
    }

    @Test
    void testFindExample() {
        //		List<UserProfile> list = service.findByExample(companyId, null, null, null, null);
        //		log.info(list.size());
        //		list.forEach(user->log.info(user.toString()));


        List<SiloUserProfile> list = service.findByExample(null, null, null, null, 1l);
        log.info(list.size());
        list.forEach(user -> log.info(user.toString()));

    }

    @Test
    void testFindByRoleIds() {
        List<SiloUserProfile> list = service.findByRoleIds(1l);
        log.info(list.size());
        list.forEach(user -> log.info(user.toString()));
    }

    @Test
    void testExists() {
        log.info(service.exists(email));
    }

    @Test
    void testDelete() {
        SiloUserProfile obj = new SiloUserProfile();
        obj.setName("測試帳號");
        obj.setEmail("aaa@bbb.ccc");
        long seq = service.add(obj);
        service.delete(seq);
        Optional<SiloUserProfile> opt = service.find(seq);
        if (opt.isPresent()) {
            fail();
        }
    }

    @Test
    void testUpdatePhones() {
        Optional<SiloUserProfile> opt = service.find(4L);
        if (opt.isPresent()) {
            SiloUserProfile obj = opt.get();
            //    		obj.setPhones(new String[]{"+886266073650", "+886979291856"});
            obj.setPhones(new String[]{"+886979291856"});
            log.info(service.update(obj).toString());
        } else {
            fail();
        }
    }

}