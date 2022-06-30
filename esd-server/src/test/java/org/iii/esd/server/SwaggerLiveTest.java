package org.iii.esd.server;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;

@SpringBootTest
@Log4j2
class SwaggerLiveTest extends AbstractTest {

    @Value("${url}")
    private String url;

    @Test
    void whenVerifySpringFoxIsWorking_thenOK() {
        String testUrl = url.concat("/v2/api-docs");
        log.info(testUrl);
        given().
                       relaxedHTTPSValidation().
                       when().
                       get(testUrl).
                       then().
                       assertThat().
                       statusCode(200)
        ;
    }
}
