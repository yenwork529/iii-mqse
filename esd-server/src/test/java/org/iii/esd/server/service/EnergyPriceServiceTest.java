package org.iii.esd.server.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.api.vo.Settlement;
import org.iii.esd.api.vo.integrate.EnergyPrice;
import org.iii.esd.mongo.document.integrate.SettlementPrice;
import org.iii.esd.mongo.repository.integrate.SettlementPriceRepository;
import org.iii.esd.mongo.service.SpinReserveService;
import org.iii.esd.server.services.EnergyPriceService;
import org.iii.esd.server.services.IntegrateBidService;
import org.iii.esd.server.wrap.SettlementWrapper;

@SpringBootTest(classes = {
        EnergyPriceService.class,
        SpinReserveService.class,
        IntegrateBidService.class,
        SettlementPriceRepository.class})
@EnableAutoConfiguration
@Log4j2
public class EnergyPriceServiceTest extends AbstractServiceTest {

    @Autowired
    private EnergyPriceService energyPriceService;
    @Autowired
    private SettlementPriceRepository repository;

    private static final LocalDate TEST_DATE = LocalDate.of(2022, 1, 6);

    @Test
    public void testSaveAndGet() {
        List<EnergyPrice> energyPriceList = prepareEnergyPrice();
        Map<String, List<EnergyPrice>> priceByDates =
                energyPriceService.preprocessEnergyPriceList(energyPriceList);
        priceByDates.forEach((priceDate, priceList) ->
                energyPriceService.updateSpinReserveBidEnergyPrice(priceDate, priceList));
        List<SettlementPrice> entityList =
                energyPriceService.getSettlementPriceByDate(TEST_DATE);
        List<Settlement> voList = entityList.stream()
                                            .map(SettlementWrapper::unwrap)
                                            .collect(Collectors.toList());

        log.info("vo: {}", voList);
    }

    private List<EnergyPrice> prepareEnergyPrice() {
        return IntStream.range(0, 24)
                        .mapToObj(hour -> {
                            LocalTime time = LocalTime.of(hour, 0, 0);
                            return EnergyPrice.builder()
                                              .localDate(TEST_DATE.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                                              .localTime(time.format(DateTimeFormatter.ofPattern("HH:mm")))
                                              .dregPrice("1")
                                              .srPrice("1")
                                              .supPrice("1")
                                              .marginalPrice("1")
                                              .build();
                        }).collect(Collectors.toList());
    }
}
