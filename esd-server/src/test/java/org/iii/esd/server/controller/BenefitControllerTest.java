package org.iii.esd.server.controller;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import org.iii.esd.server.api.response.BenefitAnalysisResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EnableAutoConfiguration
@Log4j2
class BenefitControllerTest extends AbstractControllerTest {

    @Test
    @Disabled
    void testResponse() {
        assertEquals(true, doSend());
    }

    private boolean doSend() {
        String remoteEndpoint = host + "api/benefit";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> reqEntity = new HttpEntity<String>(getPayload().toString(), headers);
        log.debug(getPayload().toString());

        boolean isPass = false;
        ResponseEntity<BenefitAnalysisResponse> respEntity = null;

        RestTemplate restTemplate = new RestTemplate();
        try {
            respEntity = restTemplate.exchange(remoteEndpoint, HttpMethod.POST, reqEntity, BenefitAnalysisResponse.class);
            log.debug(respEntity.getStatusCodeValue());

            if (respEntity.getStatusCode().is2xxSuccessful()
                    && respEntity.getBody() != null) {
                log.debug(respEntity.getBody());

                BenefitAnalysisResponse benefit = (BenefitAnalysisResponse) respEntity.getBody();
                double value = benefit.getBenefit().getSum().getDispatch_bill();
                log.debug("value=" + value);

                if (value == 6283844.6665076995) {
                    isPass = true;
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return isPass;
    }

    private JSONObject getPayload() {
        JSONObject payload = new JSONObject();
        try {
            payload.put("profile_id", 999);
            payload.put("start", "2017-01-01T00:00:00.000Z");
            payload.put("end", "2017-12-31T00:00:00.000Z");
            payload.put("data_type", 1);
        } catch (JSONException ex) {
            log.error(ex.getMessage());
            ;
        }
        return payload;
    }
}
