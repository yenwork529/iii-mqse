package org.iii.esd.mongo.service.integrate;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.exception.ApplicationException;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.CompanyProfile;
import org.iii.esd.mongo.repository.integrate.CompanyProfileRepository;
import org.iii.esd.mongo.service.AbstractServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {
        CompanyService.class,
})
@EnableAutoConfiguration
@Log4j2
public class CompanyServiceTest extends AbstractServiceTest {

    public static final String TEST_COMPANY_ID = "COM-0000-01";

    private static final String TEST_CONTRACT = "DTI";
    private static final String TEST_ADDRESS = "Taipei";
    private static final String TEST_PHONE = "02-6607-1234";
    private static final String TEST_FULL_NAME = "Institute for Information Industry";
    private static final String TEST_SHORT_NAME = "III";

    @Autowired
    private CompanyService companyService;
    @Autowired
    private CompanyProfileRepository companyRepository;

    @Test
    public void testLoadContext() {
        assertThat(companyService).isNotNull();
        assertThat(companyRepository).isNotNull();
    }

    @Test
    public void testCreateAndGet() throws WebException {
        cleanData();

        CompanyProfile company = prepareCompany();

        companyService.create(company);

        CompanyProfile current = companyService.getByCompanyId(TEST_COMPANY_ID);

        assertThat(current).isNotNull();
    }

    private void cleanData() {
        companyRepository.deleteAll();
    }

    private CompanyProfile prepareCompany() {
        return CompanyProfile.builder()
                             .companyId(TEST_COMPANY_ID)
                             .name(TEST_SHORT_NAME)
                             .fullName(TEST_FULL_NAME)
                             .phone(TEST_PHONE)
                             .address(TEST_ADDRESS)
                             .contractPerson(TEST_CONTRACT)
                             .build()
                             .initial();
    }

    @Test
    public void testCreateTheSameCompany() throws WebException {
        cleanData();

        CompanyProfile company1 = prepareCompany();

        companyService.create(company1);

        assertThrows(ApplicationException.class, ()->{
            companyService.create(company1);
        });
    }
}
