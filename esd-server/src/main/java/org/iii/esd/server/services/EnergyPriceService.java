package org.iii.esd.server.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.iii.esd.api.vo.integrate.EnergyPrice;
import org.iii.esd.mongo.document.integrate.SettlementPrice;
import org.iii.esd.mongo.repository.SpinReserveBidRepository;
import org.iii.esd.mongo.repository.SpinReserveProfileRepository;
import org.iii.esd.mongo.repository.integrate.SettlementPriceRepository;
import org.iii.esd.mongo.service.SpinReserveService;
import org.iii.esd.utils.DatetimeUtils;
import org.iii.esd.utils.TypedPair;

import static java.util.stream.Collectors.groupingBy;
import static org.iii.esd.utils.DatetimeUtils.toDate;

@Service
@Log4j2
public class EnergyPriceService {

    @Autowired
    private SpinReserveBidRepository bidRepository;
    @Autowired
    private SpinReserveProfileRepository srRepository;
    @Autowired
    private SettlementPriceRepository settlementPriceRepository;
    @Autowired
    private SpinReserveService srService;
    @Autowired
    private IntegrateBidService bidService;

    public Map<String, List<EnergyPrice>> preprocessEnergyPriceList(List<EnergyPrice> energyPrices) {
        energyPrices.forEach(EnergyPrice::trans);

        return energyPrices.stream()
                           .collect(groupingBy(EnergyPrice::getLocalDate));
    }

    public void updateSpinReserveBidEnergyPrice(String priceDate, List<EnergyPrice> priceList) {
        LocalDate priceLocalDate = LocalDate.parse(priceDate, EnergyPrice.DATE_FORMATTER);
        LocalDateTime start = priceLocalDate.atTime(0, 0, 0);
        LocalDateTime end = start.plusDays(1);

        List<SettlementPrice> origPrices = settlementPriceRepository.findByTimestamp(toDate(start), toDate(end));
        boolean updated = false;

        if (CollectionUtils.isNotEmpty(origPrices)) {
            for (SettlementPrice origPrice : origPrices) {
                for (EnergyPrice price : priceList) {
                    if (origPrice.getTimeticks() == (toDate(price.getLocalDateTime())).getTime()) {
                        origPrice.setAfcSettlementPrice(price.getDreg());
                        origPrice.setSrSettlementPrice(price.getSr());
                        origPrice.setSupSettlementPrice(price.getSup());
                        origPrice.setMarginalElectricPrice(price.getMarginal());
                        updated = true;
                    }
                }
            }

            if (updated) {
                settlementPriceRepository.saveAll(origPrices);
            }
        } else {
            for (EnergyPrice price : priceList) {
                SettlementPrice settlementPrice =
                        SettlementPrice.builder()
                                       .dt(toDate(price.getLocalDateTime()))
                                       .afcSettlementPrice(price.getDreg())
                                       .srSettlementPrice(price.getSr())
                                       .supSettlementPrice(price.getSup())
                                       .marginalElectricPrice(price.getMarginal())
                                       .build()
                                       .initial();

                settlementPriceRepository.save(settlementPrice);
            }
        }
    }

    public List<SettlementPrice> getSettlementPriceByDate(LocalDate date) {
        TypedPair<LocalDateTime> timeRange = DatetimeUtils.getStartAndEndOfDate(date);
        return settlementPriceRepository.findByTimestamp(toDate(timeRange.left()), toDate(timeRange.right()));
    }
}
