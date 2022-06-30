package org.iii.esd.server.service;

import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.repository.integrate.TxgProfileRepository;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.server.services.IntegrateElectricDataService;
import org.iii.esd.thirdparty.service.notify.LineService;

@SpringBootTest(classes = {
        TxgService.class,
        TxgProfileRepository.class,
        LineService.class})
@EnableAutoConfiguration
@Log4j2
public class LineServiceTest extends AbstractServiceTest {

    @Autowired
    private TxgService txgService;
    @Autowired
    private LineService lineService;

    private static final String TEST_TXG_ID = "IIISR1";

    @Test
    public void testSendMessage() {
        TxgProfile txg = txgService.findByTxgId(TEST_TXG_ID);

        Assertions.assertThat(txg).isNotNull();

        ApiResponse resp = lineService.sendMessage(txg.getLineToken(), "test 123!");

        Assertions.assertThat(resp).isNotNull();

        log.info(resp);
    }
}
