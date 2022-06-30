package org.iii.esd.server.wrap;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.api.vo.integrate.BidInfo;
import org.iii.esd.mongo.document.SpinReserveBid;
import org.iii.esd.mongo.document.integrate.BidResInfo;
import org.iii.esd.mongo.document.integrate.BidTxgInfo;
import org.iii.esd.mongo.repository.SpinReserveBidRepository;
import org.iii.esd.mongo.service.SpinReserveService;
import org.iii.esd.server.service.AbstractServiceTest;
import org.iii.esd.utils.DatetimeUtils;

import static org.iii.esd.utils.DatetimeUtils.toDate;

@SpringBootTest(classes = {
        SpinReserveService.class,
        SpinReserveBidRepository.class})
@EnableAutoConfiguration
@Log4j2
public class BidInfoWrapperTest extends AbstractServiceTest {

    @Autowired
    private SpinReserveService spinReserveService;

    private static final long TEST_SR_ID = 1;

    @Test
    public void testUnwrap() {
        LocalDateTime start = LocalDateTime.of(2021, 11, 8, 0, 0, 0);

        List<SpinReserveBid> spinReserveBidList = spinReserveService.findAllBySrIdAndTime(TEST_SR_ID, toDate(start));
        Assertions.assertThat(spinReserveBidList).isNotEmpty();

        List<BidInfo> bidInfoList = spinReserveBidList.stream()
                                                      .map(BidInfoWrapper::unwrap)
                                                      .collect(Collectors.toList());
        Assertions.assertThat(bidInfoList).isNotEmpty();

        log.info(bidInfoList);
    }

    private static final String TEST_TXG_ID = "TXG-001";
    private static final List<String> TEST_RES_IDS = Arrays.asList("RES-001", "RES-002");

    @Test
    public void testUnwrapFromBidInfo() {
        Date timestamp = DatetimeUtils.toDate(LocalDateTime.of(2021, 11, 8, 0, 0, 0));

        BidTxgInfo entity = BidTxgInfo.builder()
                                      .txgId(TEST_TXG_ID)
                                      .timestamp(timestamp)
                                      .awardedCapacity(BigDecimal.ZERO)
                                      .capacity(BigDecimal.ZERO)
                                      .energyPrice(BigDecimal.ZERO)
                                      .ppaCapacity(BigDecimal.ZERO)
                                      .ppaEnergyPrice(BigDecimal.ZERO)
                                      .price(BigDecimal.ZERO)
                                      .list(TEST_RES_IDS.stream().map(resId ->
                                              BidResInfo.builder()
                                                        .resId(resId)
                                                        .timestamp(timestamp)
                                                        .awardedCapacity(BigDecimal.ZERO)
                                                        .capacity(BigDecimal.ZERO)
                                                        .ppaCapacity(BigDecimal.ZERO)
                                                        .build()).collect(Collectors.toList()))
                                      .build();

        BidInfo vo = BidInfoWrapper.unwrap(entity);

        Assertions.assertThat(vo).isNotNull();

        log.info(vo);
    }
}
