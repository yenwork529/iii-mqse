package org.iii.esd.mongo.service;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {WeatherService.class})
@EnableAutoConfiguration
@Log4j2
class WeatherServiceTest extends AbstractServiceTest {

    @Value("${stationId}")
    private String stationId;

    @Autowired
    private WeatherService service;

    @Test
    void testFindDeviceStatisticsByDeviceIdAndTime() {

    }

    @Test
    void testAddWeatherData() {

    }

    @Test
    void testDelete() {

    }

}
