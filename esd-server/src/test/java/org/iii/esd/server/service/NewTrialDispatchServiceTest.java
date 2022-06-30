package org.iii.esd.server.service;

import java.time.LocalDate;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.mongo.repository.integrate.TxgDispatchEventRepository;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.server.domain.trial.DispatchEvent;
import org.iii.esd.server.services.IntegrateBidService;
import org.iii.esd.server.services.NewTrialDispatchService;

@SpringBootTest(classes = {
        NewTrialDispatchService.class,
        TxgDispatchEventRepository.class,
        IntegrateRelationService.class,
        TxgFieldService.class,
        IntegrateBidService.class})
@EnableAutoConfiguration
@Log4j2
public class NewTrialDispatchServiceTest extends AbstractServiceTest {

    @Autowired
    private NewTrialDispatchService trialDispatchService;

    @Test
    public void testLoadEvent(){
        LocalDate date = LocalDate.of(2022, 3, 7);
        String txgId = "TXG-0001-01";
        List<DispatchEvent> dispatchEvents = trialDispatchService.getEventsByDate(txgId, date);
    }
}
