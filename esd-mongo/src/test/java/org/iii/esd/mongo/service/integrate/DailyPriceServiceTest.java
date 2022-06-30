package org.iii.esd.mongo.service.integrate;


import java.time.LocalDate;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.mongo.document.integrate.DailyPrice;
import org.iii.esd.mongo.service.AbstractServiceTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        DailyPriceService.class,
})
@EnableAutoConfiguration
@Log4j2
public class DailyPriceServiceTest extends AbstractServiceTest {

    @Autowired
    private DailyPriceService service;

    @Test
    public void testGetSettlementPriceOfDay() {
        LocalDate date = LocalDate.of(2022, 3, 10);
        Optional<DailyPrice> price = service.getSettlementPriceOfDay(date);

        assertThat(price).isPresent();
    }
}
