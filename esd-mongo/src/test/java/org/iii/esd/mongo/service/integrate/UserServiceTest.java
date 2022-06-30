package org.iii.esd.mongo.service.integrate;

import java.util.Collections;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.UserProfile;
import org.iii.esd.enums.UserNoticeType;
import org.iii.esd.mongo.repository.integrate.UserProfileRepository;
import org.iii.esd.mongo.service.AbstractServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.iii.esd.Constants.ROLE_SIADMIN;

@SpringBootTest(classes = {
        UserService.class,
        UserProfileRepository.class,
})
@EnableAutoConfiguration
@Log4j2
public class UserServiceTest extends AbstractServiceTest {

    @Autowired
    private UserService service;
    @Autowired
    private UserProfileRepository repository;

    @Test
    public void testLoadContext() {
        assertThat(service).isNotNull();
        assertThat(repository).isNotNull();
    }

    @Test
    public void testCreate() throws WebException {
        repository.deleteAll();

        UserProfile user = prepareUserProfile();
        service.create(user);

        UserProfile saved = service.getByEmail(TEST_EMAIL);
        assertThat(saved).isNotNull();

        log.info(saved);
    }

    public static final String TEST_EMAIL = "admin01@iii.org.tw";

    private static final String TEST_COMPANY_ID = CompanyServiceTest.TEST_COMPANY_ID;
    private static final String TEST_NAME = "admin01";
    private static final String TEST_PASSWORD = "admin01";
    private static final String TEST_PHONE = "0266071234";
    private static final String TEST_TXG_ID = TxgServiceTest.TEST_TXG_ID;

    private UserProfile prepareUserProfile() {
        return UserProfile.builder()
                          .companyId(TEST_COMPANY_ID)
                          .email(TEST_EMAIL)
                          .name(TEST_NAME)
                          .password(TEST_PASSWORD)
                          .noticeTypes(Collections.singleton(UserNoticeType.EMAIL.getValue()))
                          .phones(new String[]{TEST_PHONE})
                          .retry(0)
                          .roleId(Long.parseLong(ROLE_SIADMIN))
                          .lineToken("")
                          .reset("")
                          .orgId(UserProfile.OrgId.builder()
                                                  .id(TEST_TXG_ID)
                                                  .type(UserProfile.OrgType.TXG)
                                                  .build())
                          .build()
                          .initial();
    }
}
