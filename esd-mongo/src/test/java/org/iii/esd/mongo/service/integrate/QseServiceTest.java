package org.iii.esd.mongo.service.integrate;

import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.QseProfile;
import org.iii.esd.mongo.repository.integrate.QseProfileRepository;
import org.iii.esd.mongo.service.AbstractServiceTest;
import org.iii.esd.mongo.service.UpdateService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        QseService.class,
        UpdateService.class,
})
@EnableAutoConfiguration
@Log4j2
public class QseServiceTest extends AbstractServiceTest {

    public static final String TEST_QSE_ID = "QSE-0000-01";

    private static final String TEST_DNP_URL = "http://iii-dnp3:8585/";
    private static final String TEST_COMPANY_ID = CompanyServiceTest.TEST_COMPANY_ID;
    private static final String TEST_WAN_IP = "140.92.24.20";
    private static final String TEST_LAN_IP = "172.10.1.1";
    private static final Integer TEST_QSE_CODE = 5076416;
    private static final String TEST_QSE_NAME = "III";

    @Autowired
    private QseService qseService;
    @Autowired
    private UpdateService updateService;
    @Autowired
    private QseProfileRepository qseRepository;

    @Test
    public void testLoadContext() {
        assertThat(qseRepository).isNotNull();
        assertThat(qseService).isNotNull();
        assertThat(updateService).isNotNull();
    }

    @Test
    public void testCreateQset() throws WebException {
        cleanQseData();

        QseProfile qse = prepareQseProfile();
        qseService.create(qse);

        List<QseProfile> qseList = qseService.getQseList();
        assertThat(qseList).isNotEmpty();
    }

    private void cleanQseData() {
        qseRepository.deleteAll();
    }

    private QseProfile prepareQseProfile() {
        return QseProfile.builder()
                         .qseId(TEST_QSE_ID)
                         .name(TEST_QSE_NAME)
                         .qseCode(TEST_QSE_CODE)
                         .dnpUrl(TEST_DNP_URL)
                         .companyId(TEST_COMPANY_ID)
                         .vpnLanIp(TEST_LAN_IP)
                         .vpnWanIp(TEST_WAN_IP)
                         .build()
                         .initial();
    }
}
