package org.iii.esd.mongo.service.integrate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

import org.iii.esd.mongo.document.integrate.DailyPrice;
import org.iii.esd.mongo.document.integrate.SettlementPrice;
import org.iii.esd.mongo.repository.integrate.DailyMarginalPriceRepository;
import org.iii.esd.mongo.repository.integrate.SettlementPriceRepository;

import static java.math.RoundingMode.HALF_UP;
import static org.iii.esd.mongo.document.integrate.DailyPrice.PRICE_SCALE;
import static org.iii.esd.utils.DatetimeUtils.toDate;

@Service
@Log4j2
public class DailyPriceService {

    @Autowired
    private SettlementPriceRepository priceRepository;
    @Autowired
    private DailyMarginalPriceRepository dailyPriceRepository;
    @Autowired
    private MongoOperations mongoOperations;

    public Optional<DailyPrice> getSettlementPriceOfDay(LocalDate date) {
        String id = DailyPrice.buildId(date);
        Optional<DailyPrice> exists = dailyPriceRepository.findById(id);

        if (exists.isPresent()) {
            return exists;
        }

        List<SettlementPrice> dailyPrices = getSettlementPriceByDate(date);
        if (CollectionUtils.isEmpty(dailyPrices)) {
            return Optional.empty();
        }

        BigDecimal avgSr = dailyPrices.stream()
                                      .map(SettlementPrice::getSrSettlementPrice)
                                      .reduce(BigDecimal.ZERO, BigDecimal::add)
                                      .divide(BigDecimal.valueOf(dailyPrices.size()), PRICE_SCALE, HALF_UP);
        BigDecimal avgSup = dailyPrices.stream()
                                       .map(SettlementPrice::getSupSettlementPrice)
                                       .reduce(BigDecimal.ZERO, BigDecimal::add)
                                       .divide(BigDecimal.valueOf(dailyPrices.size()), PRICE_SCALE, HALF_UP);
        BigDecimal avgAfc = dailyPrices.stream()
                                       .map(SettlementPrice::getAfcSettlementPrice)
                                       .reduce(BigDecimal.ZERO, BigDecimal::add)
                                       .divide(BigDecimal.valueOf(dailyPrices.size()), PRICE_SCALE, HALF_UP);
        BigDecimal avgMarginal = dailyPrices.stream()
                                            .map(SettlementPrice::getMarginalElectricPrice)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                                            .divide(BigDecimal.valueOf(dailyPrices.size()), PRICE_SCALE, HALF_UP);

        LocalDateTime start = date.atTime(0, 0, 0);
        DailyPrice dailyPrice = new DailyPrice(toDate(start), avgSr, avgSup, avgAfc, avgMarginal);
        dailyPriceRepository.save(dailyPrice);

        return Optional.of(dailyPrice);
    }

    public List<SettlementPrice> getSettlementPriceByDate(LocalDate date) {
        LocalDateTime start = date.atTime(0, 0, 0);
        LocalDateTime end = start.plusDays(1);

        return priceRepository.findByTimestampBetween(toDate(start), toDate(end));
    }
}
