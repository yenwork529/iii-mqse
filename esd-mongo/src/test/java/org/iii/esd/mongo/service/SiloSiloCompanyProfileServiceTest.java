package org.iii.esd.mongo.service;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.mongo.document.SiloCompanyProfile;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {SiloCompanyProfileService.class, UpdateService.class})
@EnableAutoConfiguration
@Log4j2
class SiloSiloCompanyProfileServiceTest extends AbstractServiceTest {

    private static final int TEST_QSE_CODE = 300;
    private static final int TEST_TXG_CODE = 310;
    private static final int TEST_SERVICE_TYPE = 3;
    private static final String TEST_DNP_URL = "http://192.168.1.53:8585";
    private static final String TEST_CALLBACK_URL = "http://192.168.1.53:8585";

    @Value("${companyId}")
    private Long id;

    @Autowired
    private SiloCompanyProfileService service;

    @Test
    // @Disabled
    void testAdd() {
        SiloCompanyProfile obj = new SiloCompanyProfile();
        //obj.setName("資策會數位所");
        obj.setName("馬雅資訊");
        obj.setQseCode(TEST_QSE_CODE);
        obj.setTgCode(TEST_TXG_CODE);
        obj.setServiceType(TEST_SERVICE_TYPE);
        obj.setDnpURL(TEST_DNP_URL);
        obj.setCallbackURL(TEST_CALLBACK_URL);
        long seq = service.add(obj);
        log.info(seq);

        Optional<SiloCompanyProfile> company = service.findByTgCode(TEST_TXG_CODE);
        assert company.isPresent();
    }

    @Test
    @Disabled
    void testUpdate() {
        Optional<SiloCompanyProfile> opt = service.find(id);
        if (opt.isPresent()) {
            SiloCompanyProfile obj = opt.get();
            obj.setName("資策會DTI");
            log.info(service.update(obj).toString());
        } else {
            fail();
        }
    }

    @Test
    void testFind() {
        Optional<SiloCompanyProfile> opt = service.find(id);
        if (opt.isPresent()) {
            log.info(opt.get().toString());
        } else {
            fail();
        }
    }

    @Test
    void testFindByExample() throws JsonProcessingException {
        List<SiloCompanyProfile> list = service.findCompanyProfileByExample(null);
        for (SiloCompanyProfile siloCompanyProfile : list) {
            ObjectMapper mapper = new ObjectMapper();
            log.info(mapper.writeValueAsString(siloCompanyProfile));
            //log.info(realTimeData.toString());
        }
    }

    @Test
    @Disabled
    void testDelete() {
        SiloCompanyProfile obj = new SiloCompanyProfile();
        obj.setName("Company Test");
        long seq = service.add(obj);
        service.delete(seq);
        Optional<SiloCompanyProfile> opt = service.find(seq);
        if (opt.isPresent()) {
            fail("delete fail.");
        }
    }

}