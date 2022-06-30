package org.iii.esd.server.controller;

import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import org.iii.esd.api.response.thinclient.ScheduleResponse;
import org.iii.esd.mongo.document.ElectricData;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EnableAutoConfiguration
@Log4j2
class ScheduleControllerTest extends AbstractControllerTest {

    @Test
    @Disabled
    void testResponse() {
        assertEquals(true, doSend());
    }

    private boolean doSend() {
        String remoteEndpoint = host + "thinclient/schedule/999?current=1505664000000";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> reqEntity = new HttpEntity<String>(null, headers);

        boolean isPass = false;
        ResponseEntity<ScheduleResponse> respEntity = null;

        RestTemplate restTemplate = new RestTemplate();
        try {
            respEntity = restTemplate.exchange(remoteEndpoint, HttpMethod.GET, reqEntity, ScheduleResponse.class);
            log.debug(respEntity.getStatusCodeValue());

            if (respEntity.getStatusCode().is2xxSuccessful()
                    && respEntity.getBody() != null) {
                log.info(respEntity.getBody());

                ScheduleResponse schedule = (ScheduleResponse) respEntity.getBody();

                List<ElectricData> electricDataList = schedule.getElectricData();
                log.info("electricDataList=" + electricDataList.toString());

                if (electricDataList != null && electricDataList.size() > 0) {
                    isPass = true;
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return isPass;
    }
}
