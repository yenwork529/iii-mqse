package org.iii.esd.server.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.api.vo.MainBoard;
import org.iii.esd.enums.NoticeType;
import org.iii.esd.mongo.document.integrate.BidResInfo;
import org.iii.esd.mongo.document.integrate.BidTxgData;
import org.iii.esd.mongo.document.integrate.BidTxgInfo;
import org.iii.esd.mongo.document.integrate.TxgDispatchEvent;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.document.integrate.UserProfile;
import org.iii.esd.mongo.repository.integrate.TxgDispatchEventRepository;
import org.iii.esd.mongo.service.integrate.IntegrateDataService;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.mongo.service.integrate.UserService;
import org.iii.esd.server.services.IntegrateBidService;
import org.iii.esd.server.services.IntegrateElectricDataService;
import org.iii.esd.server.services.MainBoardService;
import org.iii.esd.server.services.NewTrialDispatchService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.iii.esd.mongo.util.ModelHelper.asNonNull;
import static org.iii.esd.utils.DatetimeUtils.toDate;

@SpringBootTest(classes = {
        MainBoardService.class,
        IntegrateRelationService.class,
        TxgService.class,
        TxgFieldService.class,
        IntegrateElectricDataService.class,
        IntegrateDataService.class,
        IntegrateBidService.class,
        UserService.class,
        NewTrialDispatchService.class,
        TxgDispatchEventRepository.class})
@EnableAutoConfiguration
@Log4j2
public class MainBoardServiceTest extends AbstractServiceTest {

    @Autowired
    private MainBoardService mainBoardService;

    @Autowired
    private UserService userService;

    @Autowired
    private IntegrateRelationService relationService;

    @Autowired
    private IntegrateBidService bidService;

    @Autowired
    private IntegrateDataService dataService;

    @Autowired
    private TxgDispatchEventRepository eventRepo;

    private static final String TEST_TXG_ID = "TXG-0000-01";
    private static final String TEST_USER_EMAIL = "admin@iii.org.tw";
    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDateTime NOW = LocalDateTime.now();

    private static final String TEST_QSE_ID = "QSE-0000-01";

    @Test
    public void testBuildMainBoardByQseId(){
        List<MainBoard> mb = mainBoardService.buildMainBoardItemsFromQseId(TEST_QSE_ID);

        log.info("main boards: {}", mb);
    }

    /**
     * drop database first, run initializer test, and run this test after all.
     */
    @Test
    public void testMainBoardService() {
        UserProfile user = userService.findByEmail(TEST_USER_EMAIL);

        // initially default state
        checkMainBoardState(user, MainBoard.State.NOT_BIDDING);

        // bidding awarded
        prepareBidding();
        checkMainBoardState(user, MainBoard.State.STAND_BY);

        // not enough
        prepareAlert();
        checkMainBoardState(user, MainBoard.State.NOT_ENOUGH);

        // dispatch
        prepareDispatch();
        checkMainBoardState(user, MainBoard.State.DISPATCH);
    }

    private void checkMainBoardState(UserProfile user, MainBoard.State state) {
        List<MainBoard> mainBoard = mainBoardService.buildMainBoardItemsFromUser(user);
        log.info(mainBoard);

        assertThat(mainBoard).isNotEmpty();
        MainBoard first = mainBoard.get(0);
        assertThat(first.getState()).isEqualTo(state);
    }

    private void prepareAlert() {
        LocalDateTime noticeTime = NOW.minusMinutes(40L);
        TxgDispatchEvent alert = TxgDispatchEvent.builder()
                                                 .txgId(TEST_TXG_ID)
                                                 .eventName("ALERT")
                                                 .eventType("TYPE_B")
                                                 .alertType("CONSUME_NOT_ENOUGH")
                                                 .actionType("")
                                                 .eventReact(TxgDispatchEvent.EventReact.builder()
                                                                                        .reactType("NA")
                                                                                        .responseTime(new Date(0L))
                                                                                        .checkFlag("NA")
                                                                                        .reactText("NA")
                                                                                        .build())
                                                 .eventNotify(TxgDispatchEvent.EventNotify.builder()
                                                                                          .notifyType("ALERT")
                                                                                          .notifyTime(toDate(noticeTime))
                                                                                          .checkFlag(toDate(noticeTime).toString())
                                                                                          .notifyText("CONSUME_NOT_ENOUGH")
                                                                                          .build())
                                                 .serviceState("")
                                                 .eventState("CLOSED")
                                                 .createTime(toDate(noticeTime))
                                                 .updateTime(toDate(noticeTime))
                                                 .nextPoll(toDate(noticeTime))
                                                 .build();

        eventRepo.save(alert);
    }

    private void prepareDispatch() {
        TxgProfile txg = relationService.seekTxgProfileFromTxgId(TEST_TXG_ID);
        BigDecimal baseLine = BigDecimal.valueOf(8000L);
        BigDecimal clip = txg.getRegisterCapacity();
        BigDecimal clipped = baseLine.subtract(clip);
        LocalDateTime noticeTime = NOW.minusMinutes(20L);
        LocalDateTime startTime = noticeTime.plusMinutes(10L);
        LocalDateTime endTime = startTime.plusMinutes(60L);

        prepareBidData(txg, baseLine, clip, clipped, noticeTime, startTime, endTime);
        prepareDispatchEvent(txg, baseLine, clip, clipped, noticeTime, startTime, endTime);
    }

    private void prepareBidData(TxgProfile txg, BigDecimal baseLine, BigDecimal clip, BigDecimal clipped,
            LocalDateTime noticeTime, LocalDateTime startTime, LocalDateTime endTime) {
        BidTxgData bidData = BidTxgData.builder()
                                       .txgId(TEST_TXG_ID)
                                       .noticeType(NoticeType.UNLOAD)
                                       .noticeTime(toDate(noticeTime))
                                       .startTime(toDate(startTime))
                                       .endTime(toDate(endTime))
                                       .baseline(baseLine)
                                       .clipKW(txg.getRegisterCapacity().intValue())
                                       .clippedKW(baseLine.subtract(txg.getRegisterCapacity()))
                                       .timestamp(toDate(noticeTime))
                                       .timeticks(toDate(noticeTime).getTime())
                                       .updateTime(toDate(noticeTime))
                                       .build();

        dataService.save(bidData);
    }

    private void prepareDispatchEvent(TxgProfile txg, BigDecimal baseLine, BigDecimal clip, BigDecimal clipped,
            LocalDateTime noticeTime, LocalDateTime startTime, LocalDateTime endTime) {
        TxgDispatchEvent begin = TxgDispatchEvent.builder()
                                                 .txgId(TEST_TXG_ID)
                                                 .eventName("BEGIN")
                                                 .eventType("TYPE_A")
                                                 .actionType("BEGIN")
                                                 .eventParams(TxgDispatchEvent.EventParam.builder()
                                                                                         .beginTime(toDate(noticeTime))
                                                                                         .startTime(toDate(startTime))
                                                                                         .stopTime(toDate(endTime))
                                                                                         .capacity(clip.longValue())
                                                                                         .build())
                                                 .eventReact(TxgDispatchEvent.EventReact.builder()
                                                                                        .reactType("responseBegin")
                                                                                        .responseTime(toDate(noticeTime))
                                                                                        .checkFlag(toDate(noticeTime).toString())
                                                                                        .reactText("RESPONSE BEGIN")
                                                                                        .build())
                                                 .eventNotify(TxgDispatchEvent.EventNotify.builder()
                                                                                          .notifyType("BEGIN")
                                                                                          .notifyTime(toDate(noticeTime))
                                                                                          .checkFlag(toDate(noticeTime).toString())
                                                                                          .notifyText("NOTIFY BEGIN")
                                                                                          .build())
                                                 .serviceState("")
                                                 .eventState("CLOSED")
                                                 .createTime(toDate(noticeTime))
                                                 .updateTime(toDate(noticeTime))
                                                 .nextPoll(toDate(noticeTime))
                                                 .build();

        eventRepo.save(begin);
    }

    private void prepareBidding() {
        LocalDateTime now = LocalDateTime.now();

        List<BidTxgInfo> txgInfoList =
                IntStream.range(0, 24)
                         .mapToObj(hour -> {
                             TxgProfile txg = relationService.seekTxgProfileFromTxgId(TEST_TXG_ID);
                             LocalDateTime current = TODAY.atTime(hour, 0, 0);
                             List<TxgFieldProfile> resList = asNonNull(relationService.seekTxgFieldProfilesFromTxgId(TEST_TXG_ID));
                             List<BidResInfo> resInfoList = resList.stream()
                                                                   .map(res -> BidResInfo.builder()
                                                                                         .resId(res.getResId())
                                                                                         .capacity(res.getRegisterCapacity())
                                                                                         .awardedCapacity(res.getRegisterCapacity())
                                                                                         .ppaCapacity(res.getRegisterCapacity())
                                                                                         .timestamp(toDate(current))
                                                                                         .timeticks(toDate(current).getTime())
                                                                                         .createTime(toDate(now))
                                                                                         .build())
                                                                   .collect(Collectors.toList());

                             return BidTxgInfo.builder()
                                              .txgId(txg.getTxgId())
                                              .price(BigDecimal.valueOf(1))
                                              .energyPrice(BigDecimal.ZERO)
                                              .ppaEnergyPrice(BigDecimal.ZERO)
                                              .capacity(txg.getRegisterCapacity())
                                              .awardedCapacity(txg.getRegisterCapacity())
                                              .ppaCapacity(txg.getRegisterCapacity())
                                              .timestamp(toDate(current))
                                              .timeticks(toDate(current).getTime())
                                              .dt(toDate(current))
                                              .createTime(toDate(current))
                                              .updateTime(toDate(current))
                                              .list(resInfoList)
                                              .build();
                         }).collect(Collectors.toList());

        bidService.saveInfoByTxgId(TEST_TXG_ID, txgInfoList);
    }
}
