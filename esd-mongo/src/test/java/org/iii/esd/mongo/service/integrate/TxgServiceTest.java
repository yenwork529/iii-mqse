package org.iii.esd.mongo.service.integrate;

import java.math.BigDecimal;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.api.enums.ServiceType;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.repository.integrate.TxgProfileRepository;
import org.iii.esd.mongo.service.AbstractServiceTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        TxgService.class,
})
@EnableAutoConfiguration
@Log4j2
public class TxgServiceTest extends AbstractServiceTest {

    public static final String TEST_TXG_ID = "TXG-0000-01";

    private static final String TEST_QSE_ID = QseServiceTest.TEST_QSE_ID;
    private static final String TEST_COMPANY_ID = CompanyServiceTest.TEST_COMPANY_ID;
    private static final String TEST_TXG_NAME = "III-TXG";
    private static final Integer TEST_SERVICE_TYPE = ServiceType.SR.getCode();
    private static final BigDecimal TEST_CAPACITY = BigDecimal.valueOf(1000);
    private static final Integer TEST_TXG_CODE = 3;
    private static final BigDecimal TEST_EFFICIENCY_PRICE = BigDecimal.ONE;

    @Autowired
    private TxgService txgService;
    @Autowired
    private TxgProfileRepository txgRepository;

    @Test
    public void testLoadContext() {
        assertThat(txgService).isNotNull();
        assertThat(txgRepository).isNotNull();
    }

    @Test
    public void testGetAll() {
        List<TxgProfile> txgs = txgRepository.findAll();
        assertThat(txgs).isNotEmpty();
    }

    @Test
    public void testCreateAndGet() throws WebException {
        cleanTxgData();

        TxgProfile txg = prepareTxgProfile();
        txgService.create(txg);

        List<TxgProfile> txgList = txgService.getAll();
        Assertions.assertThat(txgList).isNotEmpty();
    }

    private TxgProfile prepareTxgProfile() {
        return TxgProfile.builder()
                         .txgId(TEST_TXG_ID)
                         .qseId(TEST_QSE_ID)
                         .companyId(TEST_COMPANY_ID)
                         .txgCode(TEST_TXG_CODE)
                         .name(TEST_TXG_NAME)
                         .serviceType(TEST_SERVICE_TYPE)
                         .registerCapacity(TEST_CAPACITY)
                         .efficiencyPrice(TEST_EFFICIENCY_PRICE)
                         .build()
                         .initial();
    }

    private void cleanTxgData() {
        txgRepository.deleteAll();
    }
}
