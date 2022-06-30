package org.iii.esd.mongo.service.integrate;

import java.math.BigDecimal;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.enums.ResourceType;
import org.iii.esd.mongo.repository.integrate.TxgFieldProfileRepository;
import org.iii.esd.mongo.service.AbstractServiceTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        TxgFieldService.class,
})
@EnableAutoConfiguration
@Log4j2
public class TxgFieldServiceTest extends AbstractServiceTest {

    public static final String TEST_RES_ID = "RES-0000-01";
    private static final Integer TEST_RES_CODE = 14;
    private static final String TEST_TXG_ID = TxgServiceTest.TEST_TXG_ID;
    private static final String TEST_COMPANY_ID = CompanyServiceTest.TEST_COMPANY_ID;
    private static final String TEST_NAME = "III RES";
    private static final Integer TEST_RES_TYPE = ResourceType.dr.getCode();
    private static final String TEST_TC_URL = "140.92.24.20";
    private static final BigDecimal TEST_ACCU_FACTOR = BigDecimal.ONE;
    private static final BigDecimal TEST_CAPACITY = BigDecimal.valueOf(1000);

    @Autowired
    private TxgFieldService txgFieldService;
    @Autowired
    private TxgFieldProfileRepository resRepository;

    @Test
    public void testContextLoad() {
        assertThat(txgFieldService).isNotNull();
        assertThat(resRepository).isNotNull();
    }

    @Test
    public void testGetAll() throws Exception{
        List<TxgFieldProfile> resList = txgFieldService.getAll();
        resList.forEach(log::info);
    }

    @Test
    public void testCreateAndGet() throws WebException {
        cleanData();

        TxgFieldProfile field = prepareField();
        txgFieldService.create(field);

        TxgFieldProfile field2 = txgFieldService.getByResId(field.getResId());
        assertThat(field2).isNotNull();
    }

    private TxgFieldProfile prepareField() {
        return TxgFieldProfile.builder()
                              .resId(TEST_RES_ID)
                              .resCode(TEST_RES_CODE)
                              .txgId(TEST_TXG_ID)
                              .companyId(TEST_COMPANY_ID)
                              .name(TEST_NAME)
                              .resType(TEST_RES_TYPE)
                              .tcUrl(TEST_TC_URL)
                              .tcEnable(EnableStatus.enable)
                              .accFactor(TEST_ACCU_FACTOR)
                              .registerCapacity(TEST_CAPACITY)
                              .build()
                              .initial();
    }

    private void cleanData() {
        resRepository.deleteAll();
    }
}
