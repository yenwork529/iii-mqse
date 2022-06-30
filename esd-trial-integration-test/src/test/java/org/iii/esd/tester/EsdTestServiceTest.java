package org.iii.esd.tester;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.iii.esd.common.Constant.SERVICE_TYPE_SR;

@SpringBootTest
public class EsdTestServiceTest {

    @Autowired
    private EsdTestService service;

    @Test
    public void testSendAlert() {
        AlertRequest request = AlertRequest.builder()
                                           .serverUrl("localhost:50000")
                                           .tgId("90001")
                                           .qseId("12345678")
                                           .serviceType(SERVICE_TYPE_SR)
                                           .alertTime(Instant.now())
                                           .build();
        service.sendAlert(request);
    }
}
