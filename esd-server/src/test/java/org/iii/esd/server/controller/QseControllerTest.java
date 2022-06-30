package org.iii.esd.server.controller;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.vo.integrate.Qse;
import org.iii.esd.enums.ResponseStatus;
import org.iii.esd.mongo.service.integrate.QseService;
import org.iii.esd.server.AbstractTest;
import org.iii.esd.server.controllers.rest.esd.organization.QseController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        QseController.class,
        QseService.class,},
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@EnableAutoConfiguration
@EnableScheduling
@Log4j2
public class QseControllerTest extends AbstractTest {

    @Autowired
    private QseController qseController;
    @Autowired
    private QseService qseService;

    @Test
    public void testContextLoad() {
        assertThat(qseController).isNotNull();
        assertThat(qseService).isNotNull();
    }

    private static final String TEST_QSE_NAME = "資策會";
    private static final int TEST_QSE_CODE = 5076416;
    private static final String TEST_DNP_URL = "http://iii-dnp3:8585";

    @Test
    public void testCreateQse() {
        Qse qse = new Qse();
        qse.setName(TEST_QSE_NAME);
        qse.setQseCode(TEST_QSE_CODE);
        qse.setDnpUrl(TEST_DNP_URL);

        ApiResponse response = qseController.create(qse);
        assertThat(response.getStatus()).isEqualTo(ResponseStatus.ok);
    }
}
