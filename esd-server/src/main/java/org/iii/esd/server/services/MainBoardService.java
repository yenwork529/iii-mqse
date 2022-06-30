package org.iii.esd.server.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.iii.esd.api.enums.ServiceType;
import org.iii.esd.api.vo.MainBoard;
import org.iii.esd.mongo.document.integrate.BidTxgInfo;
import org.iii.esd.mongo.document.integrate.RegulationHistory;
import org.iii.esd.mongo.document.integrate.TxgDispatchEvent;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.document.integrate.UserProfile;
import org.iii.esd.mongo.repository.integrate.RegulationHistoryRepository;
import org.iii.esd.mongo.repository.integrate.TxgDispatchEventRepository;
import org.iii.esd.mongo.service.integrate.IntegrateDataService;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.utils.GeneralPair;
import org.iii.esd.utils.PredicateUtils;
import org.iii.esd.utils.Range;

import static org.iii.esd.utils.DatetimeUtils.toDate;
import static org.iii.esd.utils.DatetimeUtils.toLocalDateTime;

@Service
public class MainBoardService {

    @Autowired
    private IntegrateRelationService relationService;
    @Autowired
    private TxgService txgService;
    @Autowired
    private TxgFieldService resService;
    @Autowired
    private IntegrateElectricDataService electricDataService;
    @Autowired
    private IntegrateDataService dataService;
    @Autowired
    private IntegrateBidService bidService;
    @Autowired
    private NewTrialDispatchService dispatchService;
    @Autowired
    private RegulationHistoryRepository regulationHistoryRepository;
    @Autowired
    private TxgDispatchEventRepository txgEventRepo;

    public List<MainBoard> buildMainBoardItemsFromUser(UserProfile user) {
        List<TxgProfile> userTxgs = getTxgProfilesOfUser(user);
        return buildMainBoardByTxgs(userTxgs);
    }

    public List<MainBoard> buildMainBoardItemsFromQseId(String qseId) {
        List<TxgProfile> allTxgs = txgService.findByQseId(qseId);
        return buildMainBoardByTxgs(allTxgs);
    }

    private List<MainBoard> buildMainBoardByTxgs(List<TxgProfile> userTxgs) {
        return userTxgs.stream()
                       .map(txg -> GeneralPair.construct(txg, buildBaseMainBoard(txg)))
                       .peek(boardPair -> {
                           buildState(boardPair);
                           buildCurrent(boardPair);
                           buildToday(boardPair);
                       })
                       .map(GeneralPair::right)
                       .collect(Collectors.toList());
    }

    private void buildToday(GeneralPair<TxgProfile, MainBoard> txgMainBoard) {
        TxgProfile txg = txgMainBoard.left();
        MainBoard mainBoard = txgMainBoard.right();

        mainBoard.setToday(getTodayOfTxg(txg));
    }

    private MainBoard.TodayIndices getTodayOfTxg(TxgProfile txg) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime todayEnd = todayStart.plusDays(1).minusSeconds(1);

        List<BidTxgInfo> todayInfo = bidService.findInfoByTxgIdAndTime(txg.getTxgId(), toDate(todayStart), toDate(todayEnd));
        List<GeneralPair<Integer, BigDecimal>> todayBidding = dispatchService.buildAwardedValuesNew(todayInfo);
        List<GeneralPair<Integer, BigDecimal>> awardedCapacity = todayBidding.stream().filter(
                                                                                     PredicateUtils.isGreaterThanZero(GeneralPair::getRight))
                                                                             .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(awardedCapacity)) {
            return MainBoard.TodayIndices.EMPTY;
        }

        List<GeneralPair<Integer, BigDecimal>> awardedPrice = buildAwardedPrice(todayInfo);

        Range<Integer> todayHourRange = Range.from(awardedCapacity.stream()
                                                                  .map(GeneralPair::left)
                                                                  .min(Integer::compareTo)
                                                                  .orElse(0))
                                             .to(awardedCapacity.stream()
                                                                .map(GeneralPair::left)
                                                                .max(Integer::compareTo)
                                                                .orElse(0));

        Range<String> todayCapacityRange = Range.from(getNumberString(awardedCapacity.stream()
                                                                                     .map(GeneralPair::right)
                                                                                     .min(BigDecimal::compareTo)
                                                                                     .orElse(BigDecimal.ZERO)))
                                                .to(getNumberString(awardedCapacity.stream()
                                                                                   .map(GeneralPair::right)
                                                                                   .max(BigDecimal::compareTo)
                                                                                   .orElse(BigDecimal.ZERO)));

        Range<String> todayPriceRange = Range.from(getNumberString(awardedPrice.stream()
                                                                               .map(GeneralPair::right)
                                                                               .filter(PredicateUtils::isNotNull)
                                                                               .min(BigDecimal::compareTo)
                                                                               .orElse(BigDecimal.ZERO)))
                                             .to(getNumberString(awardedPrice.stream()
                                                                             .map(GeneralPair::right)
                                                                             .filter(PredicateUtils::isNotNull)
                                                                             .max(BigDecimal::compareTo)
                                                                             .orElse(BigDecimal.ZERO)));

        Range<String> abandonRange = null;
        if (todayInfo.stream()
                     .anyMatch(info -> !Objects.isNull(info.getAbandon())
                             && info.getAbandon())) {

            Optional<BidTxgInfo> abandonFrom = todayInfo.stream()
                                                        .sorted(Comparator.comparing(BidTxgInfo::getTimestamp))
                                                        .filter(info -> !Objects.isNull(info.getAbandon()) && info.getAbandon())
                                                        .findFirst();
            if (abandonFrom.isPresent()) {
                abandonRange = Range.from(getTimeString(toLocalDateTime(abandonFrom.get().getAbandonFrom())))
                                    .to(getTimeString(todayEnd));
            }
        }

        return MainBoard.TodayIndices.builder()
                                     .awardedHours(awardedCapacity.size())
                                     .awardedRange(todayCapacityRange)
                                     .capacityPriceRange(todayPriceRange)
                                     .awardedPeriod(Range.from(getTimeString(todayStart.plusHours(todayHourRange.getBegin())))
                                                         .to(getTimeString(todayStart.plusHours(todayHourRange.getEnd()))))
                                     .abandonPeriod(abandonRange)
                                     .energyPriceRange(null)
                                     .build();
    }

    private List<GeneralPair<Integer, BigDecimal>> buildAwardedPrice(List<BidTxgInfo> todayInfo) {
        return todayInfo.stream()
                        .sorted(Comparator.comparing(BidTxgInfo::getTimestamp))
                        .map(bid -> GeneralPair.construct(
                                toLocalDateTime(bid.getTimestamp()).get(ChronoField.HOUR_OF_DAY),
                                bid.getPrice()))
                        .collect(Collectors.toList());
    }

    private void buildCurrent(GeneralPair<TxgProfile, MainBoard> txgMainBoard) {
        TxgProfile txg = txgMainBoard.left();
        MainBoard mainBoard = txgMainBoard.right();

        mainBoard.setCurrent(getCurrentOfTxg(txg, mainBoard));
    }

    private MainBoard.CurrentIndices getCurrentOfTxg(TxgProfile txg, MainBoard mainBoard) {
        switch (mainBoard.getState()) {
            case DISPATCH:
                return getDispatchCurrentOfTxg(txg);
            case ABANDON:
            case STAND_BY:
            case NOT_ENOUGH:
                return getStandByCurrentOfTxg(txg);
            case NOT_BIDDING:
            case NOT_AWARDED:
            default:
                return MainBoard.CurrentIndices.EMPTY;
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private MainBoard.CurrentIndices getDispatchCurrentOfTxg(TxgProfile txg) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hourStart = now.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime hourEnd = now.truncatedTo(ChronoUnit.HOURS)
                                   .plusHours(1)
                                   .minusSeconds(1);

        Optional<TxgDispatchEvent> opEvent = txgEventRepo.findCurrentTypeA(txg.getTxgId(), toDate(now));
        // TxgDispatchEvent event = txgEventRepo.findCurrentTypeA(txg.getTxgId(), toDate(now)).get();

        List<BidTxgInfo> info = bidService.findInfoByTxgIdAndTime(txg.getTxgId(), toDate(hourStart), toDate(hourEnd));
        BidTxgInfo currInfo = info.get(0);
        BigDecimal awarded = currInfo.getAwardedCapacity();

        return opEvent.map(event -> {
            TxgDispatchEvent.EventParam param = event.getEventParams();
            return MainBoard.CurrentIndices.builder()
                                           .awardedCapacity(getNumberString(awarded))
                                           .clippedCapacity(getNumberString(BigDecimal.valueOf(param.getCapacity())))
                                           .clippedPeriod(Range.from(getTimeString(toLocalDateTime(param.getStartTime())))
                                                               .to(getTimeString(toLocalDateTime(param.getStopTime()))))
                                           .noticeTime(getTimeString(toLocalDateTime(param.getBeginTime())))
                                           .build();
        }).orElse(MainBoard.CurrentIndices.builder()
                                          .awardedCapacity(getNumberString(awarded))
                                          .build());
    }

    private String getTimeString(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private MainBoard.CurrentIndices getStandByCurrentOfTxg(TxgProfile txg) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hourStart = now.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime hourEnd = now.truncatedTo(ChronoUnit.HOURS)
                                   .plusHours(1)
                                   .minusSeconds(1);

        List<BidTxgInfo> info = bidService.findInfoByTxgIdAndTime(txg.getTxgId(), toDate(hourStart), toDate(hourEnd));
        BidTxgInfo currInfo = info.get(0);
        BigDecimal awarded = currInfo.getAwardedCapacity();

        return MainBoard.CurrentIndices.builder()
                                       .awardedCapacity(getNumberString(awarded))
                                       .build();
    }

    private String getNumberString(BigDecimal value) {
        return value.stripTrailingZeros()
                    .toPlainString();
    }

    private void buildState(GeneralPair<TxgProfile, MainBoard> txgMainBoard) {
        TxgProfile txg = txgMainBoard.left();
        MainBoard mainBoard = txgMainBoard.right();

        mainBoard.setState(getStateOfTxg(txg));
    }

    public MainBoard.State getStateOfTxg(TxgProfile txg) {
        LocalDateTime now = LocalDateTime.now();
        Date nowDate = toDate(now);
        LocalDateTime hourStart = now.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime hourEnd = now.truncatedTo(ChronoUnit.HOURS)
                                   .plusHours(1)
                                   .minusSeconds(1);
        LocalDateTime hourBefore = now.minusHours(1);
        LocalDateTime nextMin = now.plusMinutes(1).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime prevMin = nextMin.minusMinutes(1);

        // check in dispatch
        ServiceType serviceType = ServiceType.ofCode(txg.getServiceType());

        if (serviceType == ServiceType.SR || serviceType == ServiceType.SUP) {
            Optional<TxgDispatchEvent> event = txgEventRepo.findCurrentTypeA(txg.getTxgId(), toDate(now));
            if (event.isPresent()) {
                return MainBoard.State.DISPATCH;
            }
        } else {
            List<RegulationHistory> regulation =
                    regulationHistoryRepository.findByTxgIdAndTimestampBetween(txg.getTxgId(), toDate(prevMin), toDate(nextMin));

            if (CollectionUtils.isNotEmpty(regulation)) {
                return MainBoard.State.DISPATCH;
            }
        }

        // check out bidding
        List<BidTxgInfo> info = bidService.findInfoByTxgIdAndTime(txg.getTxgId(), toDate(hourStart), toDate(hourEnd));
        if (CollectionUtils.isEmpty(info)) {
            return MainBoard.State.NOT_BIDDING;
        }

        BidTxgInfo currInfo = info.get(0);

        // check not awarded
        BigDecimal awarded = currInfo.getAwardedCapacity();
        if (Objects.isNull(awarded)) {
            return MainBoard.State.NOT_AWARDED;
        }

        // check abandon: has abandon && after abandonFrom
        Date abandonFrom = currInfo.getAbandonFrom();
        Boolean abandoned = currInfo.getAbandon();
        if (!Objects.isNull(abandoned)) {
            if (!Objects.isNull(abandonFrom)
                    && nowDate.after(abandonFrom)) {
                return MainBoard.State.ABANDON;
            }

            return MainBoard.State.ABANDON;
        }

        // check not enough
        List<TxgDispatchEvent> typeBs = txgEventRepo.findTypeBByTxgIdAndDateRange(txg.getTxgId(), toDate(hourBefore), nowDate);
        if (CollectionUtils.isNotEmpty(typeBs)) {
            return MainBoard.State.NOT_ENOUGH;
        }

        return MainBoard.State.STAND_BY;
    }

    private MainBoard buildBaseMainBoard(TxgProfile txg) {
        List<TxgFieldProfile> resList = resService.findByTxgId(txg.getTxgId());
        int resourceType = resList.size() == 0 ? 0 : resList.get(0).getResType();

        return MainBoard.builder()
                        .org(MainBoard.OrgInfo.builder()
                                              .txgId(txg.getTxgId())
                                              .txgName(txg.getName())
                                              .registryCapacity(txg.getRegisterCapacity())
                                              .serviceType(txg.getServiceType())
                                              .resourceType(resourceType)
                                              .resourceCount(resList.size())
                                              .build())
                        .build();
    }

    private List<TxgProfile> getTxgProfilesOfUser(UserProfile user) {
        switch (user.getOrgId().getType()) {
            case QSE:
                // return txgService.getAll();
                return txgService.findByQseId(user.getOrgId().getId());
            case TXG:
                return Collections.singletonList(txgService.findByTxgId(user.getOrgId().getId()));
            case RES:
            default:
                return Collections.emptyList();
        }
    }
}
