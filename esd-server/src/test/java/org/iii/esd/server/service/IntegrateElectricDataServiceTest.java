package org.iii.esd.server.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.api.response.ListResponse;
import org.iii.esd.api.vo.MainBoard;
import org.iii.esd.api.vo.SpinReserveHistoryData;
import org.iii.esd.api.vo.integrate.DRegData;
import org.iii.esd.enums.DataType;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.BidTxgData;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.server.services.IntegrateElectricDataService;
import org.iii.esd.server.services.MainBoardService;
import org.iii.esd.utils.JsonUtils;
import org.iii.esd.utils.TypedPair;

import static org.assertj.core.api.Assertions.assertThat;
import static org.iii.esd.server.services.IntegrateElectricDataService.getTotalMinutes;
import static org.iii.esd.server.services.IntegrateElectricDataService.getTotalSeconds;
import static org.iii.esd.utils.DatetimeUtils.DateTimeEdge.EPOCH;
import static org.iii.esd.utils.DatetimeUtils.DateTimeEdge.ETERNAL;
import static org.iii.esd.utils.DatetimeUtils.parseDateTimeOrDefault;
import static org.iii.esd.utils.DatetimeUtils.toDate;
import static org.iii.esd.utils.TypedPair.cons;

@SpringBootTest(classes = {
        TxgService.class,
        TxgFieldService.class,
        IntegrateElectricDataService.class,
        IntegrateRelationService.class})
@EnableAutoConfiguration
@Log4j2
public class IntegrateElectricDataServiceTest extends AbstractServiceTest {

    @Autowired
    private TxgService txgService;
    @Autowired
    private TxgFieldService resService;
    @Autowired
    private TxgFieldService txgFieldService;
    @Autowired
    private IntegrateElectricDataService electricDataService;
    @Autowired
    private IntegrateRelationService relationService;
    @Autowired
    private MainBoardService mainBoardService;

    private static final String TEST_TXG_ID = "TXG-0000-01";
    private static final DataType TEST_DATA_TYPE = DataType.T99;
    private static final String TEST_DATE = "2021-11-19";

    @Test
    public void testQueryElectricData() {
        TxgProfile txg = txgService.findByTxgId(TEST_TXG_ID);
        LocalDateTime startDt = LocalDate.parse(TEST_DATE).atTime(0, 0, 0);
        LocalDateTime endDt = startDt.plusDays(1);
        MainBoard.State state = mainBoardService.getStateOfTxg(txg);

        List<SpinReserveHistoryData> data =
                electricDataService.buildTxgHistoryData(state, TEST_TXG_ID, toDate(startDt), toDate(endDt), TEST_DATA_TYPE, txg);

        assertThat(data).isNotEmpty();

        log.info(data);
    }

    @Test
    public void testQueryDRegDataOfTxg() {
        TxgProfile txg = txgService.findByTxgId(TEST_TXG_ID);
        // List<TxgFieldProfile> resList = txgFieldService.findByTxgId(TEST_TXG_ID);
        List<DRegData> dRegDataList = electricDataService.getCurrentDRegDataByTxg(txg);
        // log.info("txg {}", txg);
        // log.info("dReg data {}", dRegDataList);
        log.info("dReg json \n{}", JsonUtils.serialize(new ListResponse<>(dRegDataList)));
        assertThat(dRegDataList.size()).isEqualTo(getTotalSeconds());
    }

    @Test
    public void testQueryDRegDataOfRes() {
        // TxgProfile txg = txgService.findByTxgId(TEST_TXG_ID);
        List<TxgFieldProfile> resList = txgFieldService.findByTxgId(TEST_TXG_ID);
        resList.forEach(res -> {
            List<DRegData> dRegDataList = electricDataService.getCurrentDRegDataByRes(res);
            // log.info("res {}", res);
            log.info("dReg data {}", dRegDataList);
            assertThat(dRegDataList.size()).isEqualTo(getTotalSeconds());
        });
    }

    private static final String DATE_START = "2022-01-27 10:55:00";
    // private static final String DATE_START = "2022-01-26 16:05:00";
    private static final String DATE_END = "";
    // private static final String DATE_END = "2022-01-11 16:10:00";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    public void testQueryDRegDataOfTxgByDateRange() throws WebException {
        LocalDateTime startTime = parseDateTimeOrDefault(DATE_START, FORMATTER, EPOCH);
        LocalDateTime endTime = parseDateTimeOrDefault(DATE_END, FORMATTER, ETERNAL);
        TypedPair<LocalDateTime> dateRange = cons(startTime, endTime);

        TxgProfile txg = txgService.findByTxgId(TEST_TXG_ID);
        List<DRegData> dRegDataList = electricDataService.getCurrentDRegDataByTxg(txg);
        // List<DRegData> dRegDataList = electricDataService.getDRegDataByTxgAndDateRange(txg, dateRange);
        // log.info("txg {}", txg);
        // log.info("dReg data {}", JsonUtils.serialize(dRegDataList));
        log.info("dReg data range min: {}, max: {}",
                dRegDataList.stream()
                            .min(Comparator.comparing(DRegData::getTimeticks))
                            .get(),
                dRegDataList.stream()
                            .max(Comparator.comparing(DRegData::getTimeticks))
                            .get());
        // log.info("json {}", JsonUtils.serialize(new ListResponse<>(dRegDataList)));
        assertThat(dRegDataList.size()).isEqualTo(getTotalSeconds());
    }

    @Test
    public void testQueryDRegDataOfResByDateRange() throws WebException {
        LocalDateTime startTime = parseDateTimeOrDefault(DATE_START, FORMATTER, EPOCH);
        LocalDateTime endTime = parseDateTimeOrDefault(DATE_END, FORMATTER, ETERNAL);
        TypedPair<LocalDateTime> dateRange = cons(startTime, endTime);

        List<TxgFieldProfile> resList = txgFieldService.findByTxgId(TEST_TXG_ID);
        for (TxgFieldProfile res : resList) {
            List<DRegData> dRegDataList = electricDataService.getDRegDataByResAndDateRange(res, dateRange);
            // log.info("res {}", res);
            log.info("dReg data {}", dRegDataList);
            assertThat(dRegDataList.size()).isEqualTo(getTotalSeconds());
        }
    }

    private static final String TEST_TXG_ID_2 = "TXG-0000-01";
    private static final String TEST_RES_ID_2 = "IIISR2-RES-01";
    private static final String TEST_DATE_2 = "2022-01-14";
    private static final DateTimeFormatter FORMATTER_2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    public void testQueryDRegBidDataByDate() throws WebException {
        LocalDate date = LocalDate.parse(TEST_DATE_2, FORMATTER_2);
        TxgProfile txg = txgService.findByTxgId(TEST_TXG_ID_2);

        List<DRegData> bidInfoList = electricDataService.getDRegBidInfoByTxgAndDate(txg, date);

        assertThat(bidInfoList.size()).isEqualTo(getTotalMinutes());

        // log.info("bidInfo data {}", bidInfoList);

        log.info("bidInfo data \n {}", JsonUtils.serialize(new ListResponse<>(bidInfoList)));
    }

    @Test
    public void testQueryDRegResBidDataByDate() throws WebException {
        LocalDate date = LocalDate.parse(TEST_DATE_2, FORMATTER_2);
        TxgFieldProfile res = resService.findByResId(TEST_RES_ID_2);

        List<DRegData> bidInfoList = electricDataService.getDRegBidInfoByResAndDate(res, date);

        assertThat(bidInfoList.size()).isEqualTo(getTotalMinutes());

        // log.info("bidInfo data {}", bidInfoList);

        log.info("bidInfo data \n {}", JsonUtils.serialize(new ListResponse<>(bidInfoList)));
    }

    @Test
    public void testQeuryDRegDetailData() throws WebException {
        LocalDateTime startTime = parseDateTimeOrDefault(DATE_START, FORMATTER, EPOCH);
        LocalDateTime endTime = parseDateTimeOrDefault(DATE_END, FORMATTER, ETERNAL);
        TypedPair<LocalDateTime> dateRange = cons(startTime, endTime);

        TxgProfile txg = txgService.findByTxgId(TEST_TXG_ID_2);

        List<DRegData> detailList = electricDataService.getDRegDetailByTxgAndDateRange(txg, dateRange);

        assertThat(detailList.size()).isEqualTo(getTotalSeconds());

        // log.info("detail data {}", detailList);

        log.info("detail data \n {}", JsonUtils.serialize(new ListResponse<>(detailList)));
    }

    @Test
    public void testQueryCurrentDRegDetail() throws WebException {
        TxgProfile txg = txgService.findByTxgId(TEST_TXG_ID_2);
        List<DRegData> detailList = electricDataService.getCurrentDRegDetailByTxg(txg);
        assertThat(detailList.size()).isEqualTo(getTotalSeconds());
        // log.info("detail data {}", detailList);
        log.info("detail data \n {}", JsonUtils.serialize(new ListResponse<>(detailList)));
    }

    @Test
    public void testCalculateNextTarget() {
        long testBaseLine = 4200;
        int testClip = 2000;
        long testMin1Power = 2221;
        long testMin2Power = 2323;
        long testMin3Power = 2225;

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Date nowToMinute = toDate(now);
        Date startTime = toDate(now.minusMinutes(3));
        Date endTime = toDate(now.plusMinutes(57));
        BidTxgData dispatchData = BidTxgData.builder()
                                            .baseline(BigDecimal.valueOf(testBaseLine))
                                            .clipKW(testClip)
                                            .build();
        BigDecimal target = BigDecimal.valueOf(testBaseLine - testClip);

        List<SpinReserveHistoryData> historyData = Arrays.asList(
                SpinReserveHistoryData.builder()
                                      .time(toDate(now.minusMinutes(3)))
                                      .acPower(BigDecimal.valueOf(testMin1Power))
                                      .build(),
                SpinReserveHistoryData.builder()
                                      .time(toDate(now.minusMinutes(2)))
                                      .acPower(BigDecimal.valueOf(testMin2Power))
                                      .build(),
                SpinReserveHistoryData.builder()
                                      .time(toDate(now.minusMinutes(1)))
                                      .acPower(BigDecimal.valueOf(testMin3Power))
                                      .build()
        );

        IntegrateElectricDataService.NextTargetParam param =
                IntegrateElectricDataService.NextTargetParam.builder()
                                                            .nowToMinute(nowToMinute)
                                                            .startTime(startTime)
                                                            .endTime(endTime)
                                                            .historyData(historyData)
                                                            .baseLine(dispatchData.getBaseline())
                                                            .clip(BigDecimal.valueOf(dispatchData.getClipKW()))
                                                            .build();

        int cal1Start = Instant.now().getNano();
        List<SpinReserveHistoryData> nextTarget1 = electricDataService.calculateNextTarget1(param);
        int cal1Stop = Instant.now().getNano();

        int cal2Start = Instant.now().getNano();
        List<SpinReserveHistoryData> nextTarget2 = electricDataService.calculateNextTarget2(param);
        int cal2Stop = Instant.now().getNano();

        Assertions.assertThat(nextTarget1).hasSize(2);
        Assertions.assertThat(nextTarget1.get(0)).isNotNull();
        Assertions.assertThat(nextTarget1.get(0).getNextTarget()).isNotNull();
        // Assertions.assertThat(nextTarget1.get(0)).hasFieldOrPropertyWithValue("nextTarget", target);
        Assertions.assertThat(nextTarget1.get(0).getNextTarget()).isLessThanOrEqualTo(target);
        log.info("next target 1: {}", nextTarget1.get(0));
        log.info("cal 1 time: {}", (cal1Stop - cal1Start));

        Assertions.assertThat(nextTarget2).hasSize(2);
        Assertions.assertThat(nextTarget2.get(0)).isNotNull();
        Assertions.assertThat(nextTarget2.get(0).getNextTarget()).isNotNull();
        // Assertions.assertThat(nextTarget2.get(0)).hasFieldOrPropertyWithValue("nextTarget", target);
        Assertions.assertThat(nextTarget2.get(0).getNextTarget()).isLessThanOrEqualTo(target);
        log.info("next target 2: {}", nextTarget2.get(0));
        log.info("cal 2 time: {}", (cal2Stop - cal2Start));

        Assertions.assertThat(nextTarget1.get(0).getNextTarget())
                  .isEqualTo(nextTarget2.get(0).getNextTarget());
    }
}

