package org.iii.esd.server.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import org.iii.esd.api.vo.MainBoard;
import org.iii.esd.api.vo.ResElectricData;
import org.iii.esd.api.vo.SpinReserveHistoryData;
import org.iii.esd.api.vo.SpinReserveHistoryDetailData;
import org.iii.esd.api.vo.integrate.DRegData;
import org.iii.esd.api.vo.integrate.IntegrateRealtimeData;
import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.enums.DataType;
import org.iii.esd.enums.NoticeType;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.dao.DrResDataDao;
import org.iii.esd.mongo.dao.DrTxgDataDao;
import org.iii.esd.mongo.dao.GessResDataDao;
import org.iii.esd.mongo.dao.GessTxgDataDao;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.SpinReserveBid;
import org.iii.esd.mongo.document.integrate.BidResData;
import org.iii.esd.mongo.document.integrate.BidResInfo;
import org.iii.esd.mongo.document.integrate.BidTxgData;
import org.iii.esd.mongo.document.integrate.BidTxgInfo;
import org.iii.esd.mongo.document.integrate.DrResData;
import org.iii.esd.mongo.document.integrate.GessResData;
import org.iii.esd.mongo.document.integrate.GessTxgData;
import org.iii.esd.mongo.document.integrate.RegulationHistory;
import org.iii.esd.mongo.document.integrate.TxgDeviceProfile;
import org.iii.esd.mongo.document.integrate.TxgDispatchEvent;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.enums.LoadType;
import org.iii.esd.mongo.enums.ResourceType;
import org.iii.esd.mongo.repository.integrate.BidResDataRepository;
import org.iii.esd.mongo.repository.integrate.BidTxgDataRepository;
import org.iii.esd.mongo.repository.integrate.RegulationHistoryRepository;
import org.iii.esd.mongo.repository.integrate.TxgDispatchEventRepository;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.mongo.service.SpinReserveService;
import org.iii.esd.mongo.service.StatisticsService;
import org.iii.esd.mongo.service.integrate.ConnectionService;
import org.iii.esd.mongo.service.integrate.IntegrateDataService;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.mongo.service.integrate.TxgDeviceService;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.mongo.service.integrate.UserService;
import org.iii.esd.server.domain.trial.DispatchEvent;
import org.iii.esd.server.wrap.GessDataWrapper;
import org.iii.esd.utils.DateTimeRanges;
import org.iii.esd.utils.DatetimeUtils;
import org.iii.esd.utils.GeneralPair;
import org.iii.esd.utils.JsonUtils;
import org.iii.esd.utils.MathUtils;
import org.iii.esd.utils.PredicateUtils;
import org.iii.esd.utils.TypedPair;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.FLOOR;
import static java.math.RoundingMode.HALF_UP;
import static java.util.function.Function.identity;
import static org.iii.esd.api.vo.SpinReserveHistoryData.KEY_CURR_PERFORMANCE;
import static org.iii.esd.api.vo.SpinReserveHistoryData.KEY_NEXT_TARGET;
import static org.iii.esd.api.vo.SpinReserveHistoryData.KEY_PREVIOUS_PERFORMANCE;
import static org.iii.esd.api.vo.SpinReserveHistoryData.KEY_UPDATE_TIME;
import static org.iii.esd.api.vo.SpinReserveHistoryData.SCALE_PERFORMANCE;
import static org.iii.esd.enums.NoticeType.UNLOAD;
import static org.iii.esd.mongo.util.ModelHelper.asNonNull;
import static org.iii.esd.utils.DatetimeUtils.DEFAULT_EPOCH;
import static org.iii.esd.utils.DatetimeUtils.DEFAULT_ETERNAL;
import static org.iii.esd.utils.DatetimeUtils.toDate;
import static org.iii.esd.utils.DatetimeUtils.toInstant;
import static org.iii.esd.utils.DatetimeUtils.toLocalDate;
import static org.iii.esd.utils.DatetimeUtils.toLocalDateTime;
import static org.iii.esd.utils.DatetimeUtils.truncated;
import static org.iii.esd.utils.OptionalUtils.or;
import static org.iii.esd.utils.TypedPair.cons;

@Service
@Log4j2
public class IntegrateElectricDataService {

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private DrTxgDataDao drTxgRepository;
    @Autowired
    private DrResDataDao drResRepository;
    @Autowired
    private GessResDataDao gessResDataRepository;
    @Autowired
    private GessTxgDataDao gessTxgDataRepository;
    @Autowired
    private RegulationHistoryRepository regulationHistoryRepository;
    @Autowired
    private TxgDispatchEventRepository txgEventRepository;
    @Autowired
    private BidTxgDataRepository txgDataRepository;
    @Autowired
    private BidResDataRepository resDataRepository;

    @Autowired
    private FieldProfileService fieldProfileService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private SpinReserveService spinReserveService;
    @Autowired
    private NewTrialDispatchService trialDispatchService;
    @Autowired
    private IntegrateRelationService relationService;
    @Autowired
    private IntegrateDataService dataService;
    @Autowired
    private IntegrateBidService bidService;
    @Autowired
    private TxgService txgService;
    @Autowired
    private TxgFieldService resService;
    @Autowired
    private UserService userService;
    @Autowired
    private TxgDeviceService deviceService;
    @Autowired
    private ConnectionService connService;
    @Autowired
    private MainBoardService mainBoardService;

    public List<ResElectricData> getByResListAndDataTypeAndTime(List<TxgFieldProfile> resList, DataType dataType, Date start, Date end) {
        return resList.stream()
                      .flatMap(res -> getByResAndDataTypeAndTime(res, dataType, start, end).stream())
                      .collect(Collectors.toList());
    }

    public List<ResElectricData> getByResProfileAndDataTypeAndTime(TxgFieldProfile res, DataType dataType, Date start, Date end) {
        return getByResAndDataTypeAndTime(res, dataType, start, end);
    }

    public List<SpinReserveHistoryData> buildResHistoryData(
            MainBoard.State state, String id, DataType dataType, Date start, Date end, TxgFieldProfile res) throws WebException {

        List<SpinReserveHistoryData> dataList = this.getByResAndDataTypeAndTime(res, dataType, start, end)
                                                    .stream()
                                                    .map(SpinReserveHistoryData::new)
                                                    .collect(Collectors.toList());

        TxgProfile txg = txgService.getByTxgId(res.getTxgId());
        String txgId = txg.getTxgId();

        List<BidTxgData> srdList = bidService.findDataByTxgIdAndNoticeTypeAndNoticeTime(txgId, NoticeType.UNLOAD, start, end);

        List<SpinReserveHistoryData> srhdList = new ArrayList<>();

        for (BidTxgData txgData : srdList) {
            if (isOnlyOneRes(txg)) {
                srhdList.addAll(getBaseAndTarget(txgData, BigDecimal.valueOf(1.0)));
                srhdList.addAll(getBCCtoDataList(txg, start, end, BigDecimal.valueOf(1.0)));
            } else {
                Date noticeTimeHour = truncated(txgData.getNoticeTime(), Calendar.HOUR_OF_DAY);
                Map<String, Double> ratioMap = this.calculateFieldSpinReserveBidRatioBySridAndTime(txgId, noticeTimeHour);

                log.info("fields ratios: {}", ratioMap);

                if (ratioMap.containsKey(id)) {
                    // 多場域，使用場域各自分配的 ratio 以及原本算出來的 baseline, gem 2021/05/28
                    srhdList.addAll(getBaseAndTarget(txgData, BigDecimal.valueOf(ratioMap.get(id)), res));
                    srhdList.addAll(getBCCtoDataList(txg, start, end, BigDecimal.valueOf(1.0)));
                }
            }
        }

        // 得標容量
        List<SpinReserveHistoryData> awardedData = this.getAwardedData(txg, res, start, end);
        log.info("awarded data {}", awardedData);
        srhdList.addAll(awardedData);

        // 棄標資料
        List<SpinReserveHistoryData> abandonData = getAbandonData(txg, start, awardedData);
        log.info("abandon data {}", abandonData);
        srhdList.addAll(abandonData);

        if (MainBoard.State.DISPATCH.equals(state)) {
            List<SpinReserveHistoryData> currPerf = getResCurrentPerformance(res, dataList, start, end);
            log.info("curr perf {}", currPerf);
            srhdList.addAll(currPerf);

            List<SpinReserveHistoryData> nextTarget = getResNextTarget(res, dataList, start, end);
            log.info("next target {}", nextTarget);
            srhdList.addAll(nextTarget);
        }

        mergeHistoryAndDispatch(dataList, srhdList);

        return sort(dataList);
    }

    public List<SpinReserveHistoryData> getAwardedData(TxgProfile txg, TxgFieldProfile field, Date start, Date end) {
        List<BidTxgInfo> txgInfo = bidService.findInfoByTxgIdAndTime(txg.getTxgId(), start, end);

        if (CollectionUtils.isEmpty(txgInfo)) {
            return Collections.emptyList();
        }

        BidTxgInfo dailyBegin = txgInfo.stream()
                                       .sorted(Comparator.comparing(BidTxgInfo::getTimestamp))
                                       .collect(Collectors.toList())
                                       .get(0);

        LocalDateTime beginTime = toLocalDateTime(dailyBegin.getTimestamp());

        List<GeneralPair<Integer, BigDecimal>> awardedValues = trialDispatchService.buildFieldAwardedValuesNew(txgInfo, field);

        return awardedValues.stream()
                            .filter(value -> value.right().compareTo(ZERO) > 0)
                            .flatMap(value -> this.buildAwardedHourDataPair(beginTime, value)
                                                  .stream())
                            .collect(Collectors.toList());
    }

    public Map<String, Double> calculateFieldSpinReserveBidRatioBySridAndTime(String txgId, Date time) {
        Map<String, Double> map;
        Optional<BidTxgInfo> txgInfo = bidService.findOneByTxgIdAndTime(txgId, time);

        if (txgInfo.isPresent()) {
            BigDecimal awarded_capacity = txgInfo.get().getAwardedCapacity();
            BigDecimal sr_capacity = txgInfo.get().getCapacity();

            if (awarded_capacity != null && awarded_capacity.compareTo(ZERO) > 0) {
                map = buildRatioFromAwardedCapacity(awarded_capacity, txgInfo.get());
            } else if (sr_capacity != null && sr_capacity.compareTo(ZERO) > 0) {
                map = buildRatioFromCapacity(sr_capacity, txgInfo.get());
            } else {
                map = calculateFieldNativeRatio(txgId);
            }
        } else {
            map = calculateFieldNativeRatio(txgId);
        }

        return map;
    }

    private static final int DEFAULT_SCALE = 3;

    public List<ResElectricData> getByResListAndDataTypeAndTimeGTE(List<TxgFieldProfile> resList, Date timeRange) {
        List<ResElectricData> result = new ArrayList<>();

        // 查需量反應的資料
        Set<String> drResIds = resList.stream()
                                      .filter(res -> Objects.equals(ResourceType.dr.getCode(), res.getResType()))
                                      .map(TxgFieldProfile::getResId)
                                      .collect(Collectors.toSet());

        List<DrResData> drDataList = drResRepository.findByResIdInAndTimestampGreaterThanEqual(drResIds, timeRange);

        result.addAll(drDataList.stream()
                                .map(ResElectricData::of)
                                .collect(Collectors.toList()));

        // 查儲能系統的資料
        Set<String> gessResIds = resList.stream()
                                        .filter(res -> Objects.equals(ResourceType.gess.getCode(), res.getResType()))
                                        .map(TxgFieldProfile::getResId)
                                        .collect(Collectors.toSet());

        List<GessResData> gessDataList = gessResDataRepository.findByResIdInAndTimestampGreaterThanEqual(gessResIds, timeRange);

        result.addAll(gessDataList.stream()
                                  .map(ResElectricData::of)
                                  .collect(Collectors.toList()));

        return result;
    }

    public Map<String, Object> buildTxgHistoryMeta(
            List<SpinReserveHistoryData> historyData, MainBoard.State state, TxgProfile txg, Date start) {
        if (state == MainBoard.State.DISPATCH) {
            return buildDispatchTxgHistoryMeta(historyData, txg, start);
        }

        return buildDefaultTxgHistoryMeta(txg, start);
    }

    public Map<String, Object> buildResHistoryMeta(
            List<SpinReserveHistoryData> historyData, MainBoard.State state, TxgFieldProfile res, Date start) {
        if (state == MainBoard.State.DISPATCH) {
            return buildDispatchResHistoryMeta(historyData, res, start);
        }

        return buildDefaultResHistoryMeta(res, start);
    }

    private Map<String, Object> buildDispatchTxgHistoryMeta(
            List<SpinReserveHistoryData> historyData, TxgProfile txg, Date start) {
        // 驗證日期，必須是今天
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atTime(0, 0, 0);
        LocalDateTime tomorrowStart = todayStart.plusDays(1);
        if ((start.getTime() < toDate(todayStart).getTime())
                || (toDate(tomorrowStart).getTime() <= start.getTime())) {
            log.warn("not today, get default meta.");
            return buildDefaultTxgHistoryMeta(txg, start);
        }

        // 驗證調度資料，現在必須有 UNLOAD
        LocalDateTime now = LocalDateTime.now();
        Optional<BidTxgData> mbDispatchData = txgDataRepository.findUnloadByTxgIdInCurrentTime(txg.getTxgId(), toDate(now));
        if (mbDispatchData.isEmpty()) {
            log.warn("no dispatch, get default meta.");
            return buildDefaultTxgHistoryMeta(txg, start);
        }

        // 驗證日期，已經開始調度 1 分鐘以上
        BidTxgData dispatchData = mbDispatchData.get();
        Date startTime = dispatchData.getStartTime();
        Date endTime = dispatchData.getEndTime();
        LocalDateTime lastMin = now.truncatedTo(ChronoUnit.MINUTES);
        if (toDate(lastMin).getTime() < startTime.getTime() || endTime.getTime() < toDate(lastMin).getTime()) {
            log.warn("current not dispatching, get default meta.");
            return buildDefaultTxgHistoryMeta(txg, start);
        }

        List<SpinReserveHistoryData> headHist =
                historyData.stream()
                           .filter(PredicateUtils.isBeforOrAt(SpinReserveHistoryData::getTime, toDate(lastMin)))
                           .sorted(Comparator.comparing(SpinReserveHistoryData::getTime))
                           .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(headHist)) {
            log.warn("no history data, get default meta.");
            return buildDefaultTxgHistoryMeta(txg, start);
        }

        BigDecimal cbl = dispatchData.getBaseline();
        BigDecimal clip = BigDecimal.valueOf(dispatchData.getClipKW());
        BigDecimal target = cbl.subtract(clip);

        Optional<SpinReserveHistoryData> endHist = historyData.stream()
                                                              .filter(PredicateUtils.isEqualsTo(endTime, SpinReserveHistoryData::getTime))
                                                              .findFirst();

        BigDecimal nextTarget = endHist.map(SpinReserveHistoryData::getNextTarget)
                                       .orElse(target);

        String currPerf = endHist.map(SpinReserveHistoryData::getCurrPerf)
                                 .map(BigDecimal::toString)
                                 .orElse("-");

        return ImmutableMap.<String, Object>builder()
                           .put(KEY_UPDATE_TIME, LocalTime.now()
                                                          .truncatedTo(ChronoUnit.SECONDS)
                                                          .format(UPDATE_TIME_FORMATTER))
                           .put(KEY_NEXT_TARGET, nextTarget)
                           .put(KEY_CURR_PERFORMANCE, currPerf)
                           .put(KEY_PREVIOUS_PERFORMANCE, "-")
                           .build();
    }

    private Map<String, Object> buildDispatchResHistoryMeta(
            List<SpinReserveHistoryData> historyData, TxgFieldProfile res, Date start) {
        // 驗證日期，必須是今天
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atTime(0, 0, 0);
        LocalDateTime tomorrowStart = todayStart.plusDays(1);
        if ((start.getTime() < toDate(todayStart).getTime())
                || (toDate(tomorrowStart).getTime() <= start.getTime())) {
            log.warn("not today, get default meta.");
            return buildDefaultResHistoryMeta(res, start);
        }

        // 驗證調度資料，現在必須有 UNLOAD
        LocalDateTime now = LocalDateTime.now();
        Optional<BidResData> mbDispatchData = resDataRepository.findByResIdInCurrentTime(res.getResId(), toDate(now));
        if (mbDispatchData.isEmpty()) {
            log.warn("no dispatch, get default meta.");
            return buildDefaultResHistoryMeta(res, start);
        }

        // 驗證日期，已經開始調度 1 分鐘以上
        Date startTime = mbDispatchData.get().getStartTime();
        Date endTime = mbDispatchData.get().getEndTime();
        LocalDateTime lastMin = now.truncatedTo(ChronoUnit.MINUTES);
        if (toDate(lastMin).getTime() < startTime.getTime() || endTime.getTime() < toDate(lastMin).getTime()) {
            log.warn("current not dispatching, get default meta.");
            return buildDefaultResHistoryMeta(res, start);
        }

        BidResData dispatchData = mbDispatchData.get();
        List<SpinReserveHistoryData> headHist =
                historyData.stream()
                           .filter(PredicateUtils.isBeforOrAt(SpinReserveHistoryData::getTime, toDate(lastMin)))
                           .sorted(Comparator.comparing(SpinReserveHistoryData::getTime))
                           .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(headHist)) {
            log.warn("no history data, get default meta.");
            return buildDefaultResHistoryMeta(res, start);
        }

        BigDecimal cbl = dispatchData.getBaseline();
        BigDecimal clip = BigDecimal.valueOf(dispatchData.getClipKW());
        BigDecimal target = cbl.subtract(clip);

        Optional<SpinReserveHistoryData> endHist = historyData.stream()
                                                              .filter(PredicateUtils.isEqualsTo(endTime, SpinReserveHistoryData::getTime))
                                                              .findFirst();

        BigDecimal nextTarget = endHist.map(SpinReserveHistoryData::getNextTarget)
                                       .orElse(target);

        String currPerf = endHist.map(SpinReserveHistoryData::getCurrPerf)
                                 .map(BigDecimal::toString)
                                 .orElse("-");

        return ImmutableMap.<String, Object>builder()
                           .put(KEY_UPDATE_TIME, LocalTime.now()
                                                          .truncatedTo(ChronoUnit.SECONDS)
                                                          .format(UPDATE_TIME_FORMATTER))
                           .put(KEY_NEXT_TARGET, nextTarget)
                           .put(KEY_CURR_PERFORMANCE, currPerf)
                           .put(KEY_PREVIOUS_PERFORMANCE, "-")
                           .build();
    }

    private static final DateTimeFormatter UPDATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private Map<String, Object> buildDefaultTxgHistoryMeta(TxgProfile txg, Date start) {
        Date end = toDate(toLocalDateTime(start).plusDays(1));
        Criteria crit = Criteria.where("noticeType").is(UNLOAD.name())
                                .and("txgId").is(txg.getTxgId())
                                .and("timeticks").gte(start.getTime()).lte(end.getTime());
        Query qry = new Query(crit).with(Sort.by(Sort.Direction.DESC, "timeticks"))
                                   .limit(1);
        Optional<BidTxgData> lastDispatch = Optional.ofNullable(mongoOperations.findOne(qry, BidTxgData.class));

        if (lastDispatch.isEmpty()) {
            return ImmutableMap.<String, Object>builder()
                               .put(KEY_UPDATE_TIME, LocalTime.now()
                                                              .truncatedTo(ChronoUnit.SECONDS)
                                                              .format(UPDATE_TIME_FORMATTER))
                               .put(KEY_NEXT_TARGET, "-")
                               .put(KEY_CURR_PERFORMANCE, "-")
                               .put(KEY_PREVIOUS_PERFORMANCE, "-")
                               .build();
        } else {
            String prevPerf = Optional.ofNullable(lastDispatch.get().getRevenueFactor())
                                      .map(BigDecimal::toString)
                                      .orElse("-");

            return ImmutableMap.<String, Object>builder()
                               .put(KEY_UPDATE_TIME, LocalTime.now()
                                                              .truncatedTo(ChronoUnit.SECONDS)
                                                              .format(UPDATE_TIME_FORMATTER))
                               .put(KEY_NEXT_TARGET, "-")
                               .put(KEY_CURR_PERFORMANCE, "-")
                               .put(KEY_PREVIOUS_PERFORMANCE, prevPerf)
                               .build();
        }
    }

    private Map<String, Object> buildDefaultResHistoryMeta(TxgFieldProfile res, Date start) {
        Date end = toDate(toLocalDateTime(start).plusDays(1));
        Criteria crit = Criteria.where("resId").is(res.getResId())
                                .and("timeticks").gte(start.getTime()).lte(end.getTime());
        Query qry = new Query(crit).with(Sort.by(Sort.Direction.DESC, "timeticks"))
                                   .limit(1);
        Optional<BidResData> lastDispatch = Optional.ofNullable(mongoOperations.findOne(qry, BidResData.class));

        if (lastDispatch.isEmpty()) {
            return ImmutableMap.<String, Object>builder()
                               .put(KEY_UPDATE_TIME, LocalTime.now()
                                                              .truncatedTo(ChronoUnit.SECONDS)
                                                              .format(UPDATE_TIME_FORMATTER))
                               .put(KEY_NEXT_TARGET, "-")
                               .put(KEY_CURR_PERFORMANCE, "-")
                               .put(KEY_PREVIOUS_PERFORMANCE, "-")
                               .build();
        } else {
            String prevPerf = Optional.ofNullable(lastDispatch.get().getRevenueFactor())
                                      .map(BigDecimal::toString)
                                      .orElse("-");

            return ImmutableMap.<String, Object>builder()
                               .put(KEY_UPDATE_TIME, LocalTime.now()
                                                              .truncatedTo(ChronoUnit.SECONDS)
                                                              .format(UPDATE_TIME_FORMATTER))
                               .put(KEY_NEXT_TARGET, "-")
                               .put(KEY_CURR_PERFORMANCE, "-")
                               .put(KEY_PREVIOUS_PERFORMANCE, prevPerf)
                               .build();
        }
    }

    private Map<String, Double> buildRatioFromAwardedCapacity(BigDecimal awardedCapacity, BidTxgInfo txgInfo) {
        return txgInfo.getList()
                      .stream()
                      .collect(Collectors.groupingBy(
                              BidResInfo::getResId,
                              Collectors.averagingDouble(
                                      d -> d.getAwardedCapacity()
                                            .divide(awardedCapacity, DEFAULT_SCALE, HALF_UP)
                                            .doubleValue())));
    }

    private Map<String, Double> buildRatioFromCapacity(BigDecimal capacity, BidTxgInfo txgInfo) {
        return txgInfo.getList()
                      .stream()
                      .collect(Collectors.groupingBy(
                              BidResInfo::getResId,
                              Collectors.averagingDouble(
                                      d -> d.getCapacity()
                                            .divide(capacity, DEFAULT_SCALE, HALF_UP)
                                            .doubleValue())));
    }

    private Map<String, Double> calculateFieldNativeRatio(String txgId) {
        List<TxgFieldProfile> resList = resService.findByTxgId(txgId);

        if (resList.stream()
                   .anyMatch(res ->
                           Objects.isNull(res.getRegisterCapacity()) || res.getRegisterCapacity().equals(ZERO))) {

            double ratio = BigDecimal.ONE.divide(BigDecimal.valueOf(resList.size()), DEFAULT_SCALE, HALF_UP)
                                         .doubleValue();

            return resList.stream()
                          .collect(Collectors.toMap(
                                  TxgFieldProfile::getResId, fieldProfile -> ratio));
        }

        BigDecimal capacitySum = resList.stream()
                                        .map(TxgFieldProfile::getRegisterCapacity)
                                        .reduce(ZERO, BigDecimal::add);

        return resList.stream()
                      .collect(Collectors.toMap(
                              TxgFieldProfile::getResId,
                              res -> res.getRegisterCapacity()
                                        .divide(capacitySum, DEFAULT_SCALE, HALF_UP)
                                        .doubleValue()));
    }

    private boolean isOnlyOneRes(TxgProfile txg) {
        List<TxgFieldProfile> allRes = resService.findByTxgId(txg.getTxgId());
        return CollectionUtils.isNotEmpty(allRes) && allRes.size() == 1;
    }

    private List<ResElectricData> getByResAndDataTypeAndTime(TxgFieldProfile res, DataType dataType, Date start, Date end) {
        // TODO: 加上 CGEN, UGEN, GESS 三種類型資源
        switch (res.getResType()) {
            default:
                return getDrResDataByResIdAndDataTypeAndTime(res.getResId(), dataType, start, end);
        }
    }

    private List<ResElectricData> getDrResDataByResIdAndDataTypeAndTime(String resId, DataType dataType, Date start, Date end) {
        return drResRepository.findByResIdAndTime(resId, start, end)
                              .stream()
                              .map(ResElectricData::of)
                              .collect(Collectors.toList());
    }

    public List<SpinReserveHistoryData> buildTxgHistoryExport(String txgId, Date start, Date end, DataType dataType, TxgProfile txg) {
        // 宣告回傳的資料結構
        List<SpinReserveHistoryData> dataList = new ArrayList<>();

        // 取得場域資料
        // List<FieldProfile> fieldList = fieldProfileService.findFieldProfileBySrId(txgId, EnableStatus.enable);
        List<TxgFieldProfile> resList = resService.findByTxgId(txgId);

        // 依照日期將所有場域資料加總
        if (CollectionUtils.isNotEmpty(resList)) {
            // 分別取出各場域的資料
            Map<Date, ResElectricData> dateMap =
                    this.getByResListAndDataTypeAndTime(resList, dataType, start, end)
                        .stream()
                        .collect(Collectors.groupingBy(
                                ResElectricData::getTime,
                                Collectors.reducing(new ResElectricData(),
                                        ResElectricData::sum)));

            // 各場域總和之資料內容
            List<SpinReserveHistoryData> srhList =
                    dateMap.entrySet()
                           .stream()
                           .sorted(Map.Entry.comparingByKey())
                           .map(d -> new SpinReserveHistoryData(d.getValue()))
                           .collect(Collectors.toList());

            dataList.addAll(srhList);
        }

        return dataList;
    }

    public List<SpinReserveHistoryData> buildTxgHistoryData(MainBoard.State state, String txgId, Date start, Date end, DataType dataType,
            TxgProfile txg) {
        // 宣告回傳的資料結構
        List<SpinReserveHistoryData> dataList = new ArrayList<>();

        // 宣告放置調度與契約的資料結構
        List<SpinReserveHistoryData> srhdList = new ArrayList<>();

        // 取得場域資料
        // List<FieldProfile> fieldList = fieldProfileService.findFieldProfileBySrId(txgId, EnableStatus.enable);
        List<TxgFieldProfile> resList = resService.findByTxgId(txgId);

        // 依照日期將所有場域資料加總
        if (CollectionUtils.isNotEmpty(resList)) {
            // 分別取出各場域的資料
            Map<Date, ResElectricData> dateMap =
                    this.getByResListAndDataTypeAndTime(resList, dataType, start, end)
                        .stream()
                        .collect(Collectors.groupingBy(
                                ResElectricData::getTime,
                                Collectors.reducing(new ResElectricData(),
                                        ResElectricData::sum)));

            // 各場域總和之資料內容
            List<SpinReserveHistoryData> srhList =
                    dateMap.entrySet()
                           .stream()
                           .sorted(Map.Entry.comparingByKey())
                           .map(d -> new SpinReserveHistoryData(d.getValue()))
                           .collect(Collectors.toList());

            dataList.addAll(srhList);
        }

        List<BidTxgData> srdList = bidService.findDataByTxgIdAndNoticeTypeAndNoticeTime(txgId, UNLOAD, start, end);

        for (BidTxgData spinReserveData : srdList) {
            // 抑低時基準線和降載目標
            log.info("add sr data: {}", spinReserveData);

            List<SpinReserveHistoryData> dispatchData = getBaseAndTarget(spinReserveData);
            log.info("result {}", dispatchData);

            srhdList.addAll(dispatchData);
        }

        // 競標契約容量
        List<SpinReserveHistoryData> bccData = getBCCtoDataList(txg, start, end, BigDecimal.ONE);
        log.info("bcc data {}", bccData);
        srhdList.addAll(bccData);

        // 得標容量
        List<SpinReserveHistoryData> awardedData = getAwardedData(txgId, start, end);
        log.info("awarded data {}", awardedData);
        srhdList.addAll(awardedData);

        // 棄標資料
        List<SpinReserveHistoryData> abandonData = getAbandonData(txg, start, awardedData);
        log.info("abandon data {}", abandonData);
        srhdList.addAll(abandonData);

        if (MainBoard.State.DISPATCH.equals(state)) {
            List<SpinReserveHistoryData> currPerf = getTxgCurrentPerformance(txg, dataList, start, end);
            log.info("curr perf {}", currPerf);
            srhdList.addAll(currPerf);

            List<SpinReserveHistoryData> nextTarget = getTxgNextTarget(txg, dataList, start, end);
            log.info("next target {}", nextTarget);
            srhdList.addAll(nextTarget);
        }

        mergeHistoryAndDispatch(dataList, srhdList);

        return sort(dataList);
    }

    private List<SpinReserveHistoryData> getTxgCurrentPerformance(
            TxgProfile txg, List<SpinReserveHistoryData> dataList, Date start, Date end) {
        // 驗證日期，必須是今天
        LocalDateTime now = LocalDateTime.now();
        if ((toDate(now).getTime() <= start.getTime()) || (end.getTime() <= toDate(now).getTime())) {
            return Collections.emptyList();
        }

        // 驗證事件，現在必須有 Type A 事件
        Optional<TxgDispatchEvent> event = txgEventRepository.findCurrentTypeA(txg.getTxgId(), toDate(now));
        if (event.isEmpty()) {
            return Collections.emptyList();
        }

        // 驗證調度資料，現在必須有 UNLOAD
        Optional<BidTxgData> dispatchData = txgDataRepository.findUnloadByTxgIdInCurrentTime(txg.getTxgId(), toDate(now));
        if (dispatchData.isEmpty()) {
            return Collections.emptyList();
        }

        Date nowToMinute = toDate(now.truncatedTo(ChronoUnit.MINUTES));
        Date startTime = dispatchData.get().getStartTime();
        Date endTime = dispatchData.get().getEndTime();
        List<SpinReserveHistoryData> historyData =
                dataList.stream()
                        .filter(PredicateUtils.isBetween(SpinReserveHistoryData::getTime, startTime, nowToMinute))
                        .sorted(Comparator.comparing(SpinReserveHistoryData::getTime))
                        .collect(Collectors.toList());

        return calculateCurrPerf1(NextTargetParam.builder()
                                                 .nowToMinute(nowToMinute)
                                                 .startTime(startTime)
                                                 .endTime(endTime)
                                                 .historyData(historyData)
                                                 .baseLine(dispatchData.get().getBaseline())
                                                 .clip(BigDecimal.valueOf(dispatchData.get().getClipKW()))
                                                 .build());
    }

    private List<SpinReserveHistoryData> getResCurrentPerformance(
            TxgFieldProfile res, List<SpinReserveHistoryData> dataList, Date start, Date end) {
        // 驗證日期，必須是今天
        LocalDateTime now = LocalDateTime.now();
        if ((toDate(now).getTime() <= start.getTime()) || (end.getTime() <= toDate(now).getTime())) {
            return Collections.emptyList();
        }

        // 驗證事件，現在必須有 Type A 事件
        Optional<TxgDispatchEvent> event = txgEventRepository.findCurrentTypeA(res.getTxgId(), toDate(now));
        if (event.isEmpty()) {
            return Collections.emptyList();
        }

        // 驗證調度資料，現在必須有 UNLOAD
        Optional<BidResData> dispatchData = resDataRepository.findByResIdInCurrentTime(res.getResId(), toDate(now));
        if (dispatchData.isEmpty()) {
            return Collections.emptyList();
        }

        Date nowToMinute = toDate(now.truncatedTo(ChronoUnit.MINUTES));
        Date startTime = dispatchData.get().getStartTime();
        Date endTime = dispatchData.get().getEndTime();
        List<SpinReserveHistoryData> historyData =
                dataList.stream()
                        .filter(PredicateUtils.isBetween(SpinReserveHistoryData::getTime, startTime, nowToMinute))
                        .sorted(Comparator.comparing(SpinReserveHistoryData::getTime))
                        .collect(Collectors.toList());

        return calculateCurrPerf1(NextTargetParam.builder()
                                                 .nowToMinute(nowToMinute)
                                                 .startTime(startTime)
                                                 .endTime(endTime)
                                                 .historyData(historyData)
                                                 .baseLine(dispatchData.get().getBaseline())
                                                 .clip(BigDecimal.valueOf(dispatchData.get().getClipKW()))
                                                 .build());
    }

    /**
     * <p>
     * 平均執行率計算方式 已有: <br/>
     * 1. CBL :: 基準量 (kW) <br/>
     * 2. TGP :: 目標量 (kW) <br/>
     * 3. CLIP :: 降載量 (kW) <br/>
     * 4. KWH1 :: 調度起始時間之 total kWh <br/>
     * 5. KWHn :: 調度第 n 分鐘之 total kWh <br/>
     * 6. TDP :: total dispatch period <br/>
     * 7. Mn :: 調度第 n 分鐘<br/>
     * 8. CM :: 調度已經過時間<br/>
     * </p>
     * <p>
     * 則: <br/>
     * a. CBL * CM = TKWH :: 從基準線算出來之原平均 kWh<br/>
     * b. KWHn - KWH1 = DTKWH :: 從第 n 分鐘減第 1 分鐘之 total kWh 差<br/>
     * c. CLIP * CM = DRKWH :: 調度降載之需量<br/>
     * d. TKWH - DTKWH = RUKWH :: 實際降載量<br/>
     * e. RUKWH / DRKWH = CPERF :: 至目前之平均執行率<br/>
     * </p>
     */
    public List<SpinReserveHistoryData> calculateCurrPerf1(NextTargetParam param) {
        Date nextStartTime = toDate(toLocalDateTime(param.nowToMinute).plusMinutes(1));
        int consumedMinutes = getDeltaMinutes(param.startTime, param.nowToMinute);

        BigDecimal baseLine = param.baseLine;
        BigDecimal clip = param.clip;

        BigDecimal theorykWh = baseLine.multiply(BigDecimal.valueOf(consumedMinutes));
        BigDecimal demandkWh = clip.multiply(BigDecimal.valueOf(consumedMinutes));

        List<SpinReserveHistoryData> histData = param.historyData;
        BigDecimal deltakWh = CollectionUtils.isEmpty(histData) ? ZERO :
                histData.get(histData.size() - 1)
                        .getTotalkWh()
                        .subtract(histData.get(0).getTotalkWh());

        BigDecimal realUnloadkWh = theorykWh.subtract(deltakWh);
        BigDecimal currPerf = realUnloadkWh.divide(demandkWh, SCALE_PERFORMANCE, HALF_UP)
                                           .multiply(BigDecimal.valueOf(100.00));

        log.info("curr perf {} and by param {}", currPerf, JsonUtils.serialize(param));

        return buildCurrPerfResult(TypedPair.cons(nextStartTime, param.endTime), currPerf);
    }

    // 執行率不足時，須調整至全程達成率目標值
    private static final double TRADE_OFF_PERF = 1.0;
    private static final int SCALE_POWER_0 = 0;
    private static final int SCALE_POWER_2 = 2;

    private List<SpinReserveHistoryData> getTxgNextTarget(TxgProfile txg, List<SpinReserveHistoryData> datalist, Date start, Date end) {
        // 驗證日期，必須是今天
        LocalDateTime now = LocalDateTime.now();
        if ((toDate(now).getTime() <= start.getTime()) || (end.getTime() <= toDate(now).getTime())) {
            return Collections.emptyList();
        }

        // 驗證事件，現在必須有 Type A 事件
        Optional<TxgDispatchEvent> event = txgEventRepository.findCurrentTypeA(txg.getTxgId(), toDate(now));
        if (event.isEmpty()) {
            return Collections.emptyList();
        }

        // 驗證調度資料，現在必須有 UNLOAD
        Optional<BidTxgData> dispatchData = txgDataRepository.findUnloadByTxgIdInCurrentTime(txg.getTxgId(), toDate(now));
        if (dispatchData.isEmpty()) {
            return Collections.emptyList();
        }

        Date nowToMinute = toDate(now.truncatedTo(ChronoUnit.MINUTES));
        Date startTime = dispatchData.get().getStartTime();
        Date endTime = dispatchData.get().getEndTime();
        List<SpinReserveHistoryData> historyData =
                datalist.stream()
                        .filter(PredicateUtils.isBetween(SpinReserveHistoryData::getTime, startTime, nowToMinute))
                        .sorted(Comparator.comparing(SpinReserveHistoryData::getTime))
                        .collect(Collectors.toList());

        return calculateNextTarget3(NextTargetParam.builder()
                                                   .nowToMinute(nowToMinute)
                                                   .startTime(startTime)
                                                   .endTime(endTime)
                                                   .historyData(historyData)
                                                   .baseLine(dispatchData.get().getBaseline())
                                                   .clip(BigDecimal.valueOf(dispatchData.get().getClipKW()))
                                                   .build());
    }

    private List<SpinReserveHistoryData> getResNextTarget(TxgFieldProfile res, List<SpinReserveHistoryData> datalist, Date start,
            Date end) {
        // 驗證日期，必須是今天
        LocalDateTime now = LocalDateTime.now();
        if ((toDate(now).getTime() <= start.getTime()) || (end.getTime() <= toDate(now).getTime())) {
            return Collections.emptyList();
        }

        // 驗證事件，現在必須有 Type A 事件
        Optional<TxgDispatchEvent> event = txgEventRepository.findCurrentTypeA(res.getTxgId(), toDate(now));
        if (event.isEmpty()) {
            return Collections.emptyList();
        }

        // 驗證調度資料，現在必須有 UNLOAD
        Optional<BidResData> dispatchData = resDataRepository.findByResIdInCurrentTime(res.getResId(), toDate(now));
        if (dispatchData.isEmpty()) {
            return Collections.emptyList();
        }

        Date nowToMinute = toDate(now.truncatedTo(ChronoUnit.MINUTES));
        Date startTime = dispatchData.get().getStartTime();
        Date endTime = dispatchData.get().getEndTime();
        List<SpinReserveHistoryData> historyData =
                datalist.stream()
                        .filter(PredicateUtils.isBetween(SpinReserveHistoryData::getTime, startTime, nowToMinute))
                        .sorted(Comparator.comparing(SpinReserveHistoryData::getTime))
                        .collect(Collectors.toList());

        return calculateNextTarget3(NextTargetParam.builder()
                                                   .nowToMinute(nowToMinute)
                                                   .startTime(startTime)
                                                   .endTime(endTime)
                                                   .historyData(historyData)
                                                   .baseLine(dispatchData.get().getBaseline())
                                                   .clip(BigDecimal.valueOf(dispatchData.get().getClipKW()))
                                                   .build());
    }

    /**
     * <p>
     * 建議降載量計算方式 已有: <br/>
     * 1. CBL :: 基準量 (kW) <br/>
     * 2. TGP :: 目標量 (kW) <br/>
     * 3. CLIP :: 降載量 (kW) <br/>
     * 4. KWH1 :: 調度起始時間之 total kWh <br/>
     * 5. KWHn :: 調度第 n 分鐘之 total kWh <br/>
     * 6. TDP :: total dispatch period <br/>
     * 7. RM :: 調度剩餘時間 <br/>
     * </p>
     * <p>
     * 則: <br/>
     * a. CBL * TDP = TKWH :: 平均總用量<br/>
     * b. CLIP * TDP = TUKWH :: 總降載量<br/>
     * c. TKWH - TUKWH = TGP * TDP = TDR :: 總目標需量<br/>
     * d. KWHn - KWH1 = DTKWH :: 從第 n 分鐘減第 1 分鐘之 total kWh 差<br/>
     * e. TDR - DTKWH = RKWH :: 尚需降載量<br/>
     * f. RKWH / RM = NTGP :: 建議降載量<br/>
     * g. if NTGP >= TGP then TGP else NTGP<br/>
     * </p>
     */
    public List<SpinReserveHistoryData> calculateNextTarget3(NextTargetParam param) {
        Date nextStartTime = toDate(toLocalDateTime(param.nowToMinute).plusMinutes(1));
        int totalMinutes = getDeltaMinutes(param.startTime, param.endTime);
        int consumedMinutes = getDeltaMinutes(param.startTime, param.nowToMinute);
        int remainMinutes = totalMinutes - consumedMinutes;

        BigDecimal baseLine = param.baseLine;
        BigDecimal clip = param.clip;
        BigDecimal target = baseLine.subtract(clip);

        BigDecimal totalDemand = target.multiply(BigDecimal.valueOf(totalMinutes));
        List<SpinReserveHistoryData> histData = param.historyData;
        BigDecimal deltakWh = CollectionUtils.isEmpty(histData) ? ZERO :
                histData.get(histData.size() - 1)
                        .getTotalkWh()
                        .subtract(histData.get(0).getTotalkWh());
        BigDecimal remainkWh = totalDemand.subtract(deltakWh);
        BigDecimal nextTarget = remainkWh.divide(BigDecimal.valueOf(remainMinutes), SCALE_POWER_2, HALF_UP);

        log.info("next target {}, and by param {}", nextTarget, JsonUtils.serialize(param));

        if (nextTarget.compareTo(target) >= 0) {
            return buildNextTargetResult(TypedPair.cons(nextStartTime, param.endTime), target);
        } else {
            return buildNextTargetResult(TypedPair.cons(nextStartTime, param.endTime), nextTarget);
        }
    }

    /**
     * <p>
     * 建議降載量計算方式 已有: <br/>
     * 1. TRADE_OFF_PERF :: 1.0 <br/>
     * 2. CBL :: BaseLine  <br/>
     * 3. CLP :: 原有降載量  <br/>
     * 4. TGP :: 原有目標量  <br/>
     * 5. CP :: 目前功率 <br/>
     * 6. CNT :: 筆數 <br/>
     * 7. CM :: 目前已經過之分鐘數  <br/>
     * 8. RM :: 尚未經過之分鐘數 (RM = TM - CM)
     * </p>
     * <p>
     * 則: <br/>
     * a. Σ TGP - CP = DP :: 目前功率與目標量差之總和 <br/>
     * b. if DP >= 0 then return TGP else: <br/>
     * c. CLP * RM = RP :: 尚需降載的面積量 <br/>
     * d. DP / CNT = CAP :: 目前平均 perf <br/>
     * e. CAP * CM = CTP :: 目前理論 perf <br/>
     * f. RP - CTP = RRP :: 尚需降載的實際面積量 <br/>
     * g. RRP / RM = RAP = NCLP :: 接下來每分鐘的應降載量，新的降載量 <br/>
     * h. CBL - NCLP = NTGP :: 接下來的目標量 <br/>
     * </p>
     */
    public List<SpinReserveHistoryData> calculateNextTarget2(NextTargetParam param) {
        Date nextStartTime = toDate(toLocalDateTime(param.nowToMinute).plusMinutes(1));
        int totalMinutes = getDeltaMinutes(param.startTime, param.endTime);
        int consumedMinutes = getDeltaMinutes(param.startTime, param.nowToMinute);
        int remainMinutes = totalMinutes - consumedMinutes;

        BigDecimal baseLine = param.baseLine;
        BigDecimal clip = param.clip;
        BigDecimal target = baseLine.subtract(clip);

        BigDecimal sumOfDeltaPower = ZERO; // DP
        double count = 0.0; // CNT

        for (SpinReserveHistoryData hist : param.historyData) {
            count += 1.0;

            BigDecimal currPower = hist.getAcPower();
            BigDecimal deltaPower = target.subtract(currPower);
            sumOfDeltaPower = sumOfDeltaPower.add(deltaPower);
        }

        if (sumOfDeltaPower.compareTo(ZERO) >= 0) {
            return buildNextTargetResult(TypedPair.cons(nextStartTime, param.endTime), target);
        } else {
            BigDecimal remainPower = clip.multiply(BigDecimal.valueOf(remainMinutes)); // RP
            BigDecimal currAvgPower = sumOfDeltaPower.divide(BigDecimal.valueOf(count), SCALE_POWER_2, HALF_UP); // CAP
            BigDecimal currTheoryPower = currAvgPower.multiply(BigDecimal.valueOf(consumedMinutes)); // CTP
            BigDecimal realRemainPower = remainPower.subtract(currTheoryPower); // RRP
            BigDecimal remainAvgPower = realRemainPower.divide(BigDecimal.valueOf(remainMinutes), SCALE_POWER_2, HALF_UP); // RAP
            BigDecimal nextTargetPower = baseLine.subtract(remainAvgPower).setScale(SCALE_POWER_0, FLOOR); // NTGTP

            return buildNextTargetResult(TypedPair.cons(nextStartTime, param.endTime), nextTargetPower);
        }
    }

    /**
     * <p>
     * 建議降載量計算方式 已有: <br/>
     * 1. TRADE_OFF_PERF :: 1.0 <br/>
     * 2. CBL :: BaseLine  <br/>
     * 3. CLP :: 原有降載量  <br/>
     * 4. TGP :: 原有目標量  <br/>
     * 5. CNP :: 逐筆的 perf 總合  <br/>
     * 6. CNT :: 筆數  <br/>
     * 7. TM :: 總調度分鐘數  <br/>
     * 8. CM :: 目前已經過之分鐘數  <br/>
     * 9. RM :: 尚未經過之分鐘數 (RM = TM - CM)
     * </p>
     * <p>
     * 則: <br/>
     * a. 1.0 * TM = TP :: perf 總和 <br/>
     * b. CNP / CNT = CAP :: 目前平均 perf <br/>
     * c. CAP * CM = CP :: 目前 perf 總和 <br/>
     * d. TP - CP = RP :: 尚須滿足的 perf 總和 <br/>
     * e. RP / RM = RAP :: 尚須滿足的平均 perf <br/>
     * f. CLP * RAP = NCLP :: 接下來的降載量 <br/>
     * g. CBL - NCLP = NTGT :: 接下來的目標量
     * </p>
     */
    public List<SpinReserveHistoryData> calculateNextTarget1(NextTargetParam param) {
        Date nextStartTime = toDate(toLocalDateTime(param.nowToMinute).plusMinutes(1));
        int totalMinutes = getDeltaMinutes(param.startTime, param.endTime);
        int consumedMinutes = getDeltaMinutes(param.startTime, param.nowToMinute);
        int remainMinutes = totalMinutes - consumedMinutes;

        BigDecimal baseLine = param.baseLine;
        BigDecimal clip = param.clip;
        BigDecimal target = baseLine.subtract(clip);

        GeneralPair<BigDecimal, Double> realPerfPair = calculateRealPerf(param);
        BigDecimal currRealPerf = realPerfPair.left;
        Double count = realPerfPair.right;

        // 有達標，就回傳原本的 target；沒有達標，就算出下一分鐘建議降載量 nextTarget
        if (currRealPerf.doubleValue() >= count) {
            return buildNextTargetResult(TypedPair.cons(nextStartTime, param.endTime), target);
        } else {
            BigDecimal totalPerf = BigDecimal.valueOf(TRADE_OFF_PERF).multiply(BigDecimal.valueOf(totalMinutes)); // TP
            BigDecimal avgPerf = currRealPerf.divide(BigDecimal.valueOf(count), SCALE_PERFORMANCE, HALF_UP); // CAP
            BigDecimal currPerf = avgPerf.multiply(BigDecimal.valueOf(consumedMinutes));  // CP
            BigDecimal remainPerf = totalPerf.subtract(currPerf); // RP
            BigDecimal remainAvgPerf = remainPerf.divide(BigDecimal.valueOf(remainMinutes), SCALE_PERFORMANCE, HALF_UP); // RAP
            BigDecimal nextClip = clip.multiply(remainAvgPerf); // NCLP
            BigDecimal nextTarget = baseLine.subtract(nextClip).setScale(SCALE_POWER_0, FLOOR); // NTGT

            return buildNextTargetResult(TypedPair.cons(nextStartTime, param.endTime), nextTarget);
        }
    }

    public GeneralPair<BigDecimal, Double> calculateRealPerf(NextTargetParam param) {
        BigDecimal baseLine = param.baseLine;
        BigDecimal clip = param.clip;
        BigDecimal currRealPerf = ZERO;
        double count = 0.0;

        /*
         * 計算實際執行率總和 - 1
         * 1. 執行率 > 100% 的，計算實際值，以填補未達值
         * 2. 執行率 < 0% 的，當作 0
         */
        for (SpinReserveHistoryData hist : param.historyData) {
            count += 1.0;

            BigDecimal real = hist.getAcPower();
            BigDecimal realClip = baseLine.subtract(real);

            // 若 perf <= 0 的，當作 0，即忽略不計
            if (realClip.compareTo(ZERO) > 0) {
                BigDecimal realPerf = realClip.divide(clip, SCALE_PERFORMANCE, HALF_UP);
                currRealPerf = currRealPerf.add(realPerf);
            }
        }

        /*
         * 計算實際執行率總和 - 2
         * 1. 執行率 > 100% 的，當作 100%
         * 2. 執行率 < 0% 的，當作 0
        for (SpinReserveHistoryData hist : historyData) {
            count += 1.0;

            BigDecimal real = hist.getAcPower();
            if (real.compareTo(target) <= 0) {
                // perf >= 100 的，算為 100 (1.0)
                currRealPerf = currRealPerf.add(BigDecimal.valueOf(1.0));
            } else {
                // perf < 100 的，若 perf <= 0 的，當作 0，即忽略不計
                BigDecimal realClip = baseLine.subtract(real);
                if (realClip.compareTo(ZERO) > 0) {
                    BigDecimal realPerf = realClip.divide(clip, SCALE_PERFORMANCE, HALF_UP);
                    currRealPerf = currRealPerf.add(realPerf);
                }
            }
        }
         */
        return GeneralPair.construct(currRealPerf, count);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class NextTargetParam {
        private Date nowToMinute;
        private Date startTime;
        private Date endTime;
        private List<SpinReserveHistoryData> historyData;
        private BigDecimal baseLine;
        private BigDecimal clip;
    }

    public List<SpinReserveHistoryData> buildNextTargetResult(TypedPair<Date> timePair, BigDecimal target) {
        return Arrays.asList(
                SpinReserveHistoryData.builder()
                                      .time(timePair.left())
                                      .nextTarget(target)
                                      .build(),
                SpinReserveHistoryData.builder()
                                      .time(timePair.right())
                                      .nextTarget(target)
                                      .build());
    }

    public List<SpinReserveHistoryData> buildCurrPerfResult(TypedPair<Date> timePair, BigDecimal currPerf) {
        return Arrays.asList(
                SpinReserveHistoryData.builder()
                                      .time(timePair.left())
                                      .currPerf(currPerf)
                                      .build(),
                SpinReserveHistoryData.builder()
                                      .time(timePair.right())
                                      .currPerf(currPerf)
                                      .build());
    }

    private static final long MILLIS_OF_MINUTE = 60 * 1000L;

    private int getDeltaMinutes(Date start, Date end) {
        long startTime = start.getTime();
        long endTime = end.getTime();
        long deltaTime = endTime - startTime;
        long deltaMinute = deltaTime / MILLIS_OF_MINUTE;

        return Long.valueOf(deltaMinute).intValue();
    }

    private List<SpinReserveHistoryData> getAbandonData(TxgProfile txgProfile, Date date, List<SpinReserveHistoryData> awarded) {
        if (CollectionUtils.isEmpty(awarded)) {
            return Collections.emptyList();
        }

        List<DispatchEvent> abandonEvents = trialDispatchService.getAbandonEventsByDate(txgProfile.getTxgId(), toLocalDate(date));

        log.info("abandon events {}", abandonEvents);

        return abandonEvents.stream()
                            .flatMap(event -> buildAbandonData(event, date, awarded).stream())
                            .collect(Collectors.toList());
    }

    private static final BigDecimal ABANDON_SCOPE_RATIO = BigDecimal.valueOf(1.2);

    private TypedPair<SpinReserveHistoryData> buildAbandonData(
            DispatchEvent abandon, Date dayStart, List<SpinReserveHistoryData> awarded) {
        BigDecimal abandonedAwarded = awarded.stream()
                                             .max(Comparator.comparing(SpinReserveHistoryData::getAwarded))
                                             .orElseThrow(() -> new IllegalArgumentException("awarded data is empty."))
                                             .getAwarded()
                                             .multiply(ABANDON_SCOPE_RATIO);

        Date dayEnd = toDate(toLocalDateTime(dayStart).plusDays(1).minusSeconds(1));
        Date abandonFrom = toDate(abandon.getEventParam().getAbandonFromTime());

        if (abandonFrom.after(dayStart)) {  // 在當日中止待命
            return cons(
                    SpinReserveHistoryData.getInstanceByTimeAndAbandon(abandonFrom, abandonedAwarded),
                    SpinReserveHistoryData.getInstanceByTimeAndAbandon(dayEnd, abandonedAwarded));
        } else {
            return cons(
                    SpinReserveHistoryData.getInstanceByTimeAndAbandon(dayStart, abandonedAwarded),
                    SpinReserveHistoryData.getInstanceByTimeAndAbandon(dayEnd, abandonedAwarded));
        }
    }

    public SpinReserveHistoryDetailData buildEmptyDetailData(List<TxgFieldProfile> fields, TypedPair<Date> range) {
        LocalDateTime start = toLocalDateTime(range.left());
        LocalDateTime stop = toLocalDateTime(range.left());

        List<SpinReserveHistoryDetailData.SpinReserveHistoryFieldDetail> fieldDetails =
                DateTimeRanges.ofLocalDateTime(start, stop)
                              .byMinutes()
                              .map(index -> SpinReserveHistoryDetailData.SpinReserveHistoryFieldDetail
                                      .builder()
                                      .time(toDate(index))
                                      .activePower(Collections.nCopies(fields.size(), ZERO))
                                      .build())
                              .collect(Collectors.toList());

        return SpinReserveHistoryDetailData.builder()
                                           .names(fields.stream()
                                                        .map(TxgFieldProfile::getName)
                                                        .collect(Collectors.toList()))
                                           .values(fieldDetails)
                                           .build();
    }

    public SpinReserveHistoryDetailData buildSrDetailFromFieldData(Map<TypedPair<String>, List<ResElectricData>> fieldData) {
        // 先建立場域的次序
        List<TypedPair<String>> ordered = fieldData.keySet()
                                                   .stream()
                                                   .sorted(Comparator.comparing(TypedPair::left))
                                                   .collect(Collectors.toList());

        // 將各場域 ElectricData 取出，建成時間與值的對應
        // key: electricData.time
        // value: electricData.activePower
        List<Map<Date, BigDecimal>> metaDetail =
                ordered.stream()
                       .map(fieldData::get)
                       .map(details ->
                               details.stream()
                                      .collect(Collectors.toMap(
                                              ResElectricData::getTime,
                                              ResElectricData::getActivePower)))
                       .collect(Collectors.toList());

        // 將所有場域的時間分別取出，做成集合後，排序為 List
        List<Date> timeSerial = metaDetail.stream()
                                          .flatMap(details -> details.keySet().stream())
                                          .collect(Collectors.toSet())
                                          .stream()
                                          .sorted()
                                          .collect(Collectors.toList());

        // 建置各場域的明細資料內容，以時間為 key，若無則 0
        List<SpinReserveHistoryDetailData.SpinReserveHistoryFieldDetail> detailValues =
                timeSerial.stream()
                          .map(time ->
                                  SpinReserveHistoryDetailData.SpinReserveHistoryFieldDetail
                                          .builder()
                                          .time(time)
                                          .activePower(metaDetail.stream()
                                                                 .map(detail ->
                                                                         detail.getOrDefault(time, ZERO))
                                                                 .collect(Collectors.toList()))
                                          .build())
                          .collect(Collectors.toList());

        return SpinReserveHistoryDetailData.builder()
                                           .names(ordered.stream()
                                                         .map(GeneralPair::right)
                                                         .collect(Collectors.toList()))
                                           .values(detailValues)
                                           .build();
    }

    public Map<TypedPair<String>, List<ResElectricData>> buildFieldData(
            List<TxgFieldProfile> fields, TypedPair<Date> range, DataType dataType) {

        // 先轉成 Map<TypedPair<String>, TxgFieldProfile>
        // 再用 TxgFieldProfile 的 value 去查出 ElectricData
        return fields.stream()
                     .collect(Collectors.toMap(this::fetchIndexOfFieldProfile,
                             identity()))
                     .entrySet()
                     .stream()
                     .collect(Collectors.toMap(Map.Entry::getKey, entry ->
                             range.map((start, stop) ->
                                     getByResAndDataTypeAndTime(entry.getValue(), dataType, start, stop))));
    }

    private GeneralPair<Long, String> fetchIndexOfFieldProfile(FieldProfile field) {
        return GeneralPair.construct(field.getId(), field.getName());
    }

    private TypedPair<String> fetchIndexOfFieldProfile(TxgFieldProfile res) {
        return cons(res.getResId(), res.getName());
    }

    private SpinReserveHistoryDetailData.SpinReserveHistoryFieldDetail buildFieldDetail(
            Date time, List<GeneralPair<FieldProfile, ElectricData>> fieldValues) {

        log.info("field electric data: {}", fieldValues);

        return SpinReserveHistoryDetailData.SpinReserveHistoryFieldDetail
                .builder()
                .time(time)
                .activePower(fieldValues.stream()
                                        .sorted(this::fieldComparator)  // 對應場域名稱的排序
                                        .map(value -> value.right().getActivePower())
                                        .collect(Collectors.toList()))
                .build();
    }

    private int fieldComparator(GeneralPair<FieldProfile, ?> f1, GeneralPair<FieldProfile, ?> f2) {
        return fieldComparator(f1.left(), f2.left());
    }

    private int fieldComparator(FieldProfile f1, FieldProfile f2) {
        return f1.getId().compareTo(f2.getId());
    }

    private List<SpinReserveHistoryData> sort(List<SpinReserveHistoryData> dataList) {
        return dataList.stream().
                       sorted(Comparator.comparing(SpinReserveHistoryData::getTime)).
                       collect(Collectors.toList());
    }

    private void mergeHistoryAndDispatch(List<SpinReserveHistoryData> dataList, List<SpinReserveHistoryData> srhdList) {
        if (srhdList.size() > 0) {
            srhdList.forEach(srhd -> merge(dataList, srhd));
        }
    }

    private void merge(List<SpinReserveHistoryData> dataList, SpinReserveHistoryData srhd) {
        if (dataList.stream().noneMatch(sr -> sr.getTime().equals(srhd.getTime()))) {
            dataList.add(srhd);
        } else {
            dataList.replaceAll(sr ->
                    sr.getTime()
                      .equals(srhd.getTime())
                            ? sr.merge(srhd)
                            : sr);
        }
    }

    /**
     * 競標契約容量
     *
     * @param txgProfile
     * @param start
     * @param end
     * @param ratio
     */
    private List<SpinReserveHistoryData> getBCCtoDataList(TxgProfile txgProfile, Date start, Date end, BigDecimal ratio) {
        List<SpinReserveHistoryData> list = new ArrayList<>();
        BigDecimal obcc = txgProfile.getRegisterCapacity();

        if (obcc != null) {
            Integer bcc = MathUtils.roundBigDecimal(obcc.multiply(ratio).doubleValue(), 0).intValue();

            list.add(SpinReserveHistoryData.getInstanceByTimeAndBidContractCapacity(start, bcc));
            list.add(SpinReserveHistoryData.getInstanceByTimeAndBidContractCapacity(end, bcc));
        }

        return list;
    }

    public List<SpinReserveHistoryData> getAwardedData(String txgId, Date start, Date end) {
        List<BidTxgInfo> bidData = bidService.findInfoByTxgIdAndTime(txgId, start, end);

        if (CollectionUtils.isEmpty(bidData)) {
            return Collections.emptyList();
        }

        BidTxgInfo dailyBegin = bidData.stream()
                                       .sorted(Comparator.comparing(BidTxgInfo::getTimestamp))
                                       .collect(Collectors.toList())
                                       .get(0);

        LocalDateTime beginTime = toLocalDateTime(dailyBegin.getTimestamp());

        List<GeneralPair<Integer, BigDecimal>> awardedValues = trialDispatchService.buildAwardedValuesNew(bidData);

        return awardedValues.stream()
                            .filter(value -> value.right().compareTo(ZERO) > 0)
                            .flatMap(value -> this.buildAwardedHourDataPair(beginTime, value)
                                                  .stream())
                            .collect(Collectors.toList());
    }

    private TypedPair<SpinReserveHistoryData> buildAwardedHourDataPair(
            LocalDateTime beginTime, GeneralPair<Integer, BigDecimal> awardedValue) {
        LocalDateTime startTime = beginTime.plusHours(awardedValue.left());
        LocalDateTime stopTime = startTime.plusHours(1).minusMinutes(1);

        return cons(
                this.buildAwardedHistoryData(startTime, mWtoKW(awardedValue.right())),
                this.buildAwardedHistoryData(stopTime, mWtoKW(awardedValue.right())));
    }

    public BigDecimal mWtoKW(BigDecimal mW) {
        return mW.multiply(BigDecimal.valueOf(1000L));
    }

    private SpinReserveHistoryData buildAwardedHistoryData(LocalDateTime time, BigDecimal value) {
        return SpinReserveHistoryData.builder()
                                     .time(DatetimeUtils.toDate(time))
                                     .awarded(value)
                                     .build();
    }

    private SpinReserveHistoryData buildAwardedHistoryData(LocalDateTime timestamp, List<SpinReserveBid> bidData) {
        LocalDateTime display = timestamp.truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime bidHour = timestamp.truncatedTo(ChronoUnit.HOURS);
        Date bidTime = toDate(bidHour);

        return bidData.stream()
                      .filter(bid -> bid.getTimestamp().equals(bidTime))
                      .findFirst()
                      .map(bid -> new SpinReserveHistoryData(toDate(display), bid.getAwarded_capacity()))
                      .orElseThrow(() ->
                              new IllegalArgumentException("cannot find hour at bid data: " + bidHour.get(ChronoField.HOUR_OF_DAY)));
    }

    private SpinReserveHistoryData buildAwardedHistoryDataOfField(LocalDateTime timestamp, List<SpinReserveBid> bidData,
            FieldProfile field) {
        LocalDateTime display = timestamp.truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime bidHour = timestamp.truncatedTo(ChronoUnit.HOURS);
        Date bidTime = toDate(bidHour);

        return bidData.stream()
                      .filter(bid -> bid.getTimestamp().equals(bidTime))
                      .findFirst()
                      .map(bid -> bid.getList()
                                     .stream()
                                     .filter(bidDetail ->
                                             !Objects.isNull(bidDetail.getFieldProfile())
                                                     && field.getId().equals(bidDetail.getFieldProfile().getId()))
                                     .findFirst()
                                     .map(bidDetail ->
                                             SpinReserveHistoryData.getInstanceByTimeAndAwarded(
                                                     toDate(display), bidDetail.getAwarded_capacity()))
                                     .orElseThrow(() ->
                                             new IllegalArgumentException("cannot find field id: " + field.getId())))
                      .orElseThrow(() ->
                              new IllegalArgumentException("cannot find hour at bid data: " + bidHour.get(ChronoField.HOUR_OF_DAY)));
    }

    /**
     * 抑低時基準線和降載目標
     */
    private List<SpinReserveHistoryData> getBaseAndTarget(BidTxgData txgData) {
        List<SpinReserveHistoryData> list = new ArrayList<>();

        BigDecimal obase = txgData.getBaseline().multiply(BigDecimal.ONE);
        BigDecimal base = obase.multiply(BigDecimal.ONE);
        BigDecimal clip = new BigDecimal(txgData.getClipKW());
        BigDecimal otarget = obase.subtract(clip);
        BigDecimal target = ZERO.compareTo(otarget) > 0 ? ZERO : otarget.multiply(BigDecimal.ONE);

        list.add(new SpinReserveHistoryData(DatetimeUtils.add(txgData.getNoticeTime(), Calendar.MINUTE, -5), base, null));
        list.add(new SpinReserveHistoryData(txgData.getStartTime(), null, target, clip));
        list.add(new SpinReserveHistoryData(txgData.getEndTime(), base, target, clip));

        return list;
    }

    private List<SpinReserveHistoryData> getBaseAndTarget(BidTxgData txgData, BigDecimal ratio) {
        List<SpinReserveHistoryData> list = new ArrayList<>();

        BigDecimal obase = txgData.getBaseline().multiply(ratio);
        BigDecimal base = obase.multiply(ratio);
        BigDecimal clip = new BigDecimal(txgData.getClipKW());
        BigDecimal otarget = obase.subtract(clip);
        BigDecimal target = ZERO.compareTo(otarget) > 0 ? ZERO : otarget.multiply(ratio);
        list.add(new SpinReserveHistoryData(DatetimeUtils.add(txgData.getNoticeTime(), Calendar.MINUTE, -5), base, null));
        list.add(new SpinReserveHistoryData(txgData.getStartTime(), null, target, clip));
        list.add(new SpinReserveHistoryData(txgData.getEndTime(), base, target, clip));

        return list;
    }

    private List<SpinReserveHistoryData> getBaseAndTarget(BidTxgData txgData, BigDecimal ratio, TxgFieldProfile res) {
        List<SpinReserveHistoryData> list = new ArrayList<>();

        txgData.getList()
               .stream()
               .filter(d -> Objects.equals(d.getResId(), res.getResId()))
               .findFirst()
               .ifPresent(fieldData -> {
                   BigDecimal base = fieldData.getBaseline();
                   Date baseTime = DatetimeUtils.add(txgData.getNoticeTime(), Calendar.MINUTE, -5);
                   list.add(SpinReserveHistoryData.getInstanceByTimeAndBase(baseTime, base));

                   BigDecimal clipped = BigDecimal.valueOf(txgData.getClipKW()).multiply(ratio);
                   BigDecimal usTarget = base.subtract(clipped);
                   BigDecimal sTarget = usTarget.compareTo(ZERO) < 0 ? ZERO : usTarget;
                   list.add(SpinReserveHistoryData.getInstanceByTimeAndTargetClip(
                           txgData.getStartTime(), sTarget, clipped));

                   list.add(SpinReserveHistoryData.getInstanceByTimeAndBaseTargetClip(
                           txgData.getEndTime(), base, sTarget, clipped));
               });

        return list;
    }

    public List<IntegrateRealtimeData> getRealTimeDataByResId(String resId) throws WebException {
        TxgFieldProfile res = resService.getByResId(resId);

        TxgDeviceProfile device = deviceService.findByResId(resId)
                                               .stream()
                                               .filter(TxgDeviceProfile::isMainLoad)
                                               .findFirst()
                                               .orElseThrow(() -> new WebException(Error.noData, resId));

        Long lastDeviceReportTime = connService.getLastTicks(device.getId());
        device.setReportTime(new Date(lastDeviceReportTime));

        device.setConnectionStatus(checkDevConnectionStatus(device));

        if (Objects.equals(ResourceType.dr.getCode(), res.getResType())) {
            Optional<DrResData> resData = drResRepository.findTopByResIdOrderByTimestampDesc(resId);

            return Collections.singletonList(resData.isEmpty() ?
                    IntegrateRealtimeData.buildFromDeviceProfile(device)
                    : IntegrateRealtimeData.buildFromDataAndDevice(resData.get(), device));
        } else if (Objects.equals(ResourceType.gess.getCode(), res.getResType())) {
            Optional<GessResData> resData = gessResDataRepository.findTopByResIdOrderByTimestampDesc(resId);

            return Collections.singletonList(resData.isEmpty() ?
                    IntegrateRealtimeData.buildFromDeviceProfile(device)
                    : IntegrateRealtimeData.buildFromDataAndDevice(resData.get(), device));
        } else {
            throw new WebException(Error.serviceUnsupported);
        }
    }

    private ConnectionStatus checkDevConnectionStatus(TxgDeviceProfile dev) {
        return connService.isDevcieConnected(dev) ? ConnectionStatus.Connected : ConnectionStatus.Disconnected;
    }

    public static final int DREG_RANGE = 10;
    public static final int DREG_LATENCY = 30;
    public static final int SECONDS_OF_MINUTE = 60;
    public static final int HOURS_OF_DAY = 24;
    public static final int MINUTES_OF_HOUR = 60;
    public static final BigDecimal MW_TO_KW = BigDecimal.valueOf(1000);

    // TODO: remove fake baseFreq
    public static final double FAKE_BASE_REQ = 60.0;

    private TypedPair<LocalDateTime> buildDRegDateRange(LocalDateTime now) {
        LocalDateTime endTime = now.truncatedTo(ChronoUnit.SECONDS).minusSeconds(DREG_LATENCY);
        LocalDateTime startTime = endTime.minusMinutes(DREG_RANGE);

        return cons(startTime, endTime);
    }

    public List<DRegData> getCurrentDRegDataByTxg(TxgProfile txg) {
        LocalDateTime now = LocalDateTime.now();

        TypedPair<LocalDateTime> dateTimeRange = buildDRegDateRange(now);
        log.info("get dreg from {} to {}", dateTimeRange.left(), dateTimeRange.right());

        Optional<TxgDeviceProfile> essDeviceProfile = getEssDeviceProfileByTxg(txg);
        BigDecimal ratedVoltage = getEssRatedVoltage(essDeviceProfile);
        log.info("get rated voltage of {} is {}", txg.getTxgId(), ratedVoltage);

        List<GessTxgData> entities = this.getGessDataByTxgAndDateRange(txg, dateTimeRange);
        log.info("get dreg entitie size: {}", entities.size());

        List<RegulationHistory> historyList = this.getDRegHistoryByTxgAndDateRange(txg, dateTimeRange);
        log.info("get regulation history: {}", historyList);

        return this.buildDRegDataFromTxgEntitiesAndHistory(entities, historyList, dateTimeRange, ratedVoltage);
    }

    private List<RegulationHistory> getDRegHistoryByTxgAndDateRange(TxgProfile txg, TypedPair<LocalDateTime> dateTimeRange) {
        LocalDateTime startTime = dateTimeRange.left();
        LocalDateTime endTime = dateTimeRange.right();

        return regulationHistoryRepository.findByTxgIdAndTimestampBetween(txg.getTxgId(), toDate(startTime), toDate(endTime));
    }

    public List<GessTxgData> getGessDataByTxgAndDateRange(TxgProfile txg, TypedPair<LocalDateTime> dateTimeRange) {
        LocalDateTime startTime = dateTimeRange.left();
        LocalDateTime endTime = dateTimeRange.right();

        return gessTxgDataRepository.findByTxgIdAndTimestampBetween(txg.getTxgId(), toDate(startTime), toDate(endTime));
    }

    private static final BigDecimal BASE_FREQ_RATIO = BigDecimal.valueOf(100.00);
    private static final int BASE_FREQ_SCALE = 2;

    public List<DRegData> buildDRegDataFromTxgEntitiesAndHistory(List<GessTxgData> entities, List<RegulationHistory> historyList,
            TypedPair<LocalDateTime> dateTimeRange, BigDecimal ratedVoltage) {
        LocalDateTime startTime = dateTimeRange.left();
        int totalSeconds = getTotalSeconds();

        return IntStream.range(0, totalSeconds)
                        .mapToObj(sec -> {
                            LocalDateTime currTime = startTime.plusSeconds(sec);
                            long currTimesec = toInstant(currTime).getEpochSecond();
                            long currTimeMilli = currTimesec * 1000L;
                            GessTxgData entity = entities.stream()
                                                         .filter(ent -> getDataTimeSec(ent.getTimestamp()) == currTimesec)
                                                         .findFirst()
                                                         .orElse(GessTxgData.builder()
                                                                            .timestamp(new Date(currTimeMilli))
                                                                            .build());
                            return GessDataWrapper.unwrapTxgDataWithRatedVoltage(entity, ratedVoltage);
                        })
                        .peek(dRegData -> {
                            LocalDateTime dataTime = toLocalDateTime(dRegData.getTimeticks());
                            int dataMin = dataTime.getMinute();

                            historyList.forEach(hist -> {
                                LocalDateTime histDt = toLocalDateTime(hist.getTargetFreqTime());
                                // LocalDateTime histDt = toLocalDateTime(hist.getTimestamp());
                                int histMin = histDt.getMinute();

                                if (dataMin == histMin) {
                                    dRegData.setBaseFreq(BigDecimal.valueOf(hist.getTargetFrequency())
                                                                   .divide(BASE_FREQ_RATIO, BASE_FREQ_SCALE, HALF_UP));
                                }
                            });
                        }).collect(Collectors.toList());
    }

    private long getDataTimeSec(Date timestamp) {
        return toInstant(timestamp).getEpochSecond();
    }

    public List<DRegData> getCurrentDRegDataByRes(TxgFieldProfile res) {
        LocalDateTime now = LocalDateTime.now();

        Optional<TxgDeviceProfile> essDeviceProfile = getM3Device(res);
        BigDecimal ratedVoltage = getEssRatedVoltage(essDeviceProfile);
        log.info("get rated voltage of {} is {}", res.getResId(), ratedVoltage);

        TypedPair<LocalDateTime> dateTimeRange = buildDRegDateRange(now);
        List<GessResData> entities = this.getGessDataByResAndDateRange(res, dateTimeRange);

        return this.buildDRegDataFromResEntities(entities, dateTimeRange, ratedVoltage);
    }

    private List<GessResData> getGessDataByResAndDateRange(TxgFieldProfile res, TypedPair<LocalDateTime> dateTimeRange) {
        LocalDateTime startTime = dateTimeRange.left();
        LocalDateTime endTime = dateTimeRange.right();

        return gessResDataRepository.findByResIdAndTimestampBetween(res.getResId(), toDate(startTime), toDate(endTime));
    }

    public List<DRegData> buildDRegDataFromResEntities(List<GessResData> entities, TypedPair<LocalDateTime> dateTimeRange,
            BigDecimal ratedVoltage) {
        LocalDateTime startTime = dateTimeRange.left();
        int totalSeconds = getTotalSeconds();

        return IntStream.range(0, totalSeconds)
                        .mapToObj(sec -> {
                            LocalDateTime currTime = startTime.plusSeconds(sec);
                            long currTimesec = toInstant(currTime).getEpochSecond();
                            long currTimeMilli = currTimesec * 1000L;
                            GessResData entity = entities.stream()
                                                         .filter(ent -> getDataTimeSec(ent.getTimestamp()) == currTimesec)
                                                         .findFirst()
                                                         .orElse(GessResData.builder()
                                                                            .timestamp(new Date(currTimeMilli)).build());
                            return GessDataWrapper.unwrapResDataWithRatedVoltage(entity, ratedVoltage);
                        }).collect(Collectors.toList());
    }

    public static int getTotalSeconds() {
        return (DREG_RANGE * SECONDS_OF_MINUTE) + 1;
    }

    public static int getTotalMinutes() {
        return (MINUTES_OF_HOUR * HOURS_OF_DAY);
    }

    public List<DRegData> getDRegDataByTxgAndDateRange(TxgProfile txg, TypedPair<LocalDateTime> dateRange) throws WebException {
        TypedPair<LocalDateTime> fixedDateRange = fixDateRange(dateRange);
        log.info("get dreg from {} to {}", fixedDateRange.left(), fixedDateRange.right());

        Optional<TxgDeviceProfile> essDeviceProfile = getEssDeviceProfileByTxg(txg);
        BigDecimal ratedVoltage = getEssRatedVoltage(essDeviceProfile);
        log.info("get rated voltage of {} is {}", txg.getTxgId(), ratedVoltage);

        List<GessTxgData> entities = this.getGessDataByTxgAndDateRange(txg, fixedDateRange);
        log.info("get dreg entitie size: {}", entities.size());

        List<RegulationHistory> historyList = this.getDRegHistoryByTxgAndDateRange(txg, fixedDateRange);

        return this.buildDRegDataFromTxgEntitiesAndHistory(entities, historyList, fixedDateRange, ratedVoltage);
    }

    private static final BigDecimal DEFAULT_RATED_VOLTAGE = BigDecimal.valueOf(1.0);

    private BigDecimal getEssRatedVoltage(Optional<TxgDeviceProfile> essDeviceProfile) {
        return essDeviceProfile.isPresent() ? or(essDeviceProfile.get()
                                                                 .getSetupData()
                                                                 .getRatedVoltage(), DEFAULT_RATED_VOLTAGE)
                : DEFAULT_RATED_VOLTAGE;
    }

    private Optional<TxgDeviceProfile> getEssDeviceProfileByTxg(TxgProfile txg) {
        List<TxgFieldProfile> resList = asNonNull(relationService.seekTxgFieldProfilesFromTxgId(txg.getTxgId()));
        Optional<TxgFieldProfile> essRes = resList.stream()
                                                  .filter(res -> Objects.equals(ResourceType.gess.getCode(), res.getResType()))
                                                  .findFirst();
        if (essRes.isEmpty()) {
            return Optional.empty();
        }

        return getM3Device(essRes.get());
    }

    public Optional<TxgDeviceProfile> getM3Device(TxgFieldProfile essRes) {
        List<TxgDeviceProfile> deviceList = deviceService.findByResId(essRes.getResId());

        return deviceList.stream()
                         .filter(device -> Objects.equals(LoadType.M3, device.getLoadType()))
                         .findFirst();
    }

    private TypedPair<LocalDateTime> fixDateRange(TypedPair<LocalDateTime> dateRange) throws WebException {
        checkDateRange(dateRange);

        LocalDateTime start = dateRange.left();
        LocalDateTime end = dateRange.right();

        TypedPair<LocalDateTime> defaultRange = buildDRegDateRange(LocalDateTime.now());

        if (toInstant(start).equals(DEFAULT_EPOCH) && toInstant(end).equals(DEFAULT_ETERNAL)) {
            return defaultRange;
        } else if (toInstant(start).equals(DEFAULT_EPOCH)) {
            return buildNewStart(dateRange, defaultRange);
        } else if (toInstant(end).equals(DEFAULT_ETERNAL)) {
            return buildNewEnd(dateRange, defaultRange);
        } else {
            return dateRange;
        }
    }

    private TypedPair<LocalDateTime> buildNewEnd(TypedPair<LocalDateTime> dateRange, TypedPair<LocalDateTime> defaultRange)
            throws WebException {
        checkStart(dateRange, defaultRange);

        LocalDateTime start = dateRange.left();
        LocalDateTime newEnd = start.plusMinutes(DREG_RANGE);

        return cons(start, newEnd);
    }

    private TypedPair<LocalDateTime> buildNewStart(TypedPair<LocalDateTime> dateRange, TypedPair<LocalDateTime> defaultRange)
            throws WebException {
        checkEnd(dateRange, defaultRange);

        LocalDateTime end = dateRange.right();
        LocalDateTime newStart = end.minusMinutes(DREG_RANGE);

        return cons(newStart, end);
    }

    private void checkDateRange(TypedPair<LocalDateTime> dateRange) throws WebException {
        checkSequence(dateRange);
        checkInRange(dateRange);
    }

    private void checkSequence(TypedPair<LocalDateTime> dateRange) throws WebException {
        LocalDateTime start = dateRange.left();
        LocalDateTime end = dateRange.right();

        if (toInstant(end).toEpochMilli() <= toInstant(start).toEpochMilli()) {
            throw new WebException(Error.invalidParameter, "dateRange");
        }
    }

    private void checkInRange(TypedPair<LocalDateTime> dateRange) throws WebException {
        LocalDateTime start = dateRange.left();
        LocalDateTime end = dateRange.right();

        if (toInstant(start).equals(DEFAULT_EPOCH) || toInstant(end).equals(DEFAULT_ETERNAL)) {
            return;
        }

        long diff = toInstant(end).getEpochSecond() - toInstant(start).getEpochSecond();
        long totalSec = getTotalSeconds();

        if (totalSec < diff) {
            throw new WebException(Error.invalidParameter, "dateRange");
        }
    }

    private void checkStart(TypedPair<LocalDateTime> dateRange, TypedPair<LocalDateTime> defaultRange) throws WebException {
        LocalDateTime start = dateRange.left();
        LocalDateTime end = dateRange.right();
        LocalDateTime defaultStart = defaultRange.left();

        if (toInstant(end).equals(DEFAULT_ETERNAL) &&
                toInstant(defaultStart).getEpochSecond() < toInstant(start).getEpochSecond()) {
            throw new WebException(Error.invalidParameter, "dateStart");
        }
    }

    private void checkEnd(TypedPair<LocalDateTime> dateRange, TypedPair<LocalDateTime> defaultRange) throws WebException {
        LocalDateTime start = dateRange.left();
        LocalDateTime end = dateRange.right();
        LocalDateTime defaultEnd = defaultRange.right();

        if (toInstant(start).equals(DEFAULT_EPOCH) &&
                toInstant(defaultEnd).getEpochSecond() < toInstant(end).getEpochSecond()) {
            throw new WebException(Error.invalidParameter, "dateEnd");
        }
    }

    public List<DRegData> getDRegDataByResAndDateRange(TxgFieldProfile res, TypedPair<LocalDateTime> dateRange) throws WebException {
        TypedPair<LocalDateTime> fixedDateRange = fixDateRange(dateRange);

        Optional<TxgDeviceProfile> essDeviceProfile = getM3Device(res);
        BigDecimal ratedVoltage = getEssRatedVoltage(essDeviceProfile);
        log.info("get rated voltage of {} is {}", res.getResId(), ratedVoltage);

        List<GessResData> entities = this.getGessDataByResAndDateRange(res, fixedDateRange);

        return this.buildDRegDataFromResEntities(entities, fixedDateRange, ratedVoltage);
    }

    public List<DRegData> getDRegBidInfoByTxgAndDate(TxgProfile txg, LocalDate date) {
        LocalDateTime start = date.atTime(0, 0, 0);

        List<BidTxgInfo> bidInfo = bidService.findInfoByTxgIdAndTime(txg.getTxgId(), toDate(start));
        Map<Integer, BidTxgInfo> bidInfoByHour = bidInfo.stream()
                                                        .collect(Collectors.toMap(
                                                                info -> toLocalDateTime(info.getTimestamp()).getHour(), identity()));

        return IntStream.range(0, getTotalMinutes())
                        .mapToObj(minOfDay -> {
                            LocalDateTime curr = start.plusMinutes(minOfDay);
                            return DRegData.builderOfTimeticks(toDate(curr).getTime())
                                           .build();
                        })
                        .peek(dregData -> {
                            if (MapUtils.isNotEmpty(bidInfoByHour)) {
                                LocalDateTime curr = toLocalDateTime(dregData.getTimeticks());
                                int dataHour = curr.getHour();
                                // int dataDay = curr.getDayOfYear();

                                if (bidInfoByHour.containsKey(dataHour)) {
                                    BidTxgInfo info = bidInfoByHour.get(dataHour);
                                    dregData.setAwardedCapacity(or(info.getAwardedCapacity(), ZERO).multiply(MW_TO_KW));
                                    dregData.setAbandon(getAbandonValue(info));
                                }
                            }
                        }).collect(Collectors.toList());
    }

    private BigDecimal getAbandonValue(BidTxgInfo info) {
        if (or(info.getAbandon(), Boolean.FALSE)) {
            return info.getAwardedCapacity();
        }

        return null;
    }

    public List<DRegData> getDRegBidInfoByResAndDate(TxgFieldProfile res, LocalDate date) {
        LocalDateTime start = date.atTime(0, 0, 0);

        List<BidTxgInfo> bidInfo = bidService.findInfoByTxgIdAndTime(res.getTxgId(), toDate(start));
        Map<Integer, BidTxgInfo> bidInfoByHour = bidInfo.stream()
                                                        .collect(Collectors.toMap(
                                                                info -> toLocalDateTime(info.getTimestamp()).getHour(), identity()));

        return IntStream.range(0, getTotalMinutes())
                        .mapToObj(minOfDay -> {
                            LocalDateTime curr = start.plusMinutes(minOfDay);
                            return DRegData.builderOfTimeticks(toDate(curr).getTime())
                                           .build();
                        })
                        .peek(dregData -> {
                            if (MapUtils.isNotEmpty(bidInfoByHour)) {
                                LocalDateTime curr = toLocalDateTime(dregData.getTimeticks());
                                int dataHour = curr.getHour();
                                // int dataDay = curr.getDayOfYear();

                                if (bidInfoByHour.containsKey(dataHour)) {
                                    BidTxgInfo info = bidInfoByHour.get(dataHour);
                                    Optional<BidResInfo> resInfo = info.getList()
                                                                       .stream()
                                                                       .filter(item -> item.getResId().equals(res.getResId()))
                                                                       .findFirst();

                                    if (resInfo.isPresent()) {
                                        dregData.setAwardedCapacity(or(resInfo.get()
                                                                              .getAwardedCapacity(), ZERO).multiply(MW_TO_KW));
                                        dregData.setAbandon(getAbandonValue(info, resInfo.get()));
                                    }
                                }
                            }
                        }).collect(Collectors.toList());
    }

    private BigDecimal getAbandonValue(BidTxgInfo info, BidResInfo resInfo) {
        if (or(info.getAbandon(), Boolean.FALSE)) {
            return resInfo.getAwardedCapacity();
        }

        return null;
    }

    public List<DRegData> getDRegDetailByTxgAndDateRange(TxgProfile txg, TypedPair<LocalDateTime> dateRange) throws WebException {
        TypedPair<LocalDateTime> fixedDateRange = fixDateRange(dateRange);

        List<TxgFieldProfile> resList = resService.findByTxgId(txg.getTxgId());

        return this.buildDRegDetailFromResList(resList, fixedDateRange);
    }

    private List<DRegData> buildDRegDetailFromResList(List<TxgFieldProfile> resList, TypedPair<LocalDateTime> fixedDateRange) {
        LocalDateTime start = fixedDateRange.left();
        List<DRegData.ResDetail> empty =
                resList.stream()
                       .map(res -> DRegData.ResDetail.builder()
                                                     .resId(res.getResId())
                                                     .build())
                       .collect(Collectors.toList());

        List<DRegData> baseData =
                resList.stream()
                       .flatMap(res -> getGessDataByResAndDateRange(res, fixedDateRange).stream())
                       .collect(Collectors.groupingBy(GessResData::getTimestamp))
                       .entrySet()
                       .stream()
                       .map(entry -> {
                           Date timestamp = entry.getKey();
                           List<GessResData> entities = entry.getValue();

                           List<DRegData.ResDetail> resDetails =
                                   entities.stream()
                                           .map(data ->
                                                   DRegData.ResDetail.builder()
                                                                     .resId(data.getResId())
                                                                     .kw(data.getM1kW())
                                                                     .build())
                                           .collect(Collectors.toList());

                           return DRegData.builderOfTimeticks(timestamp.getTime())
                                          .res(resDetails)
                                          .build();
                       })
                       .sorted(Comparator.comparing(DRegData::getTimeticks))
                       .collect(Collectors.toList());

        return IntStream.range(0, getTotalSeconds())
                        .mapToObj(sec -> {
                            LocalDateTime curr = start.plusSeconds(sec);
                            long currTimesec = toInstant(curr).getEpochSecond();
                            long currTimeMilli = currTimesec * 1000L;

                            return baseData.stream()
                                           .filter(data -> data.getTimeticks() == currTimeMilli)
                                           .findFirst()
                                           .orElse(DRegData.builderOfTimeticks(currTimeMilli)
                                                           .res(empty)
                                                           .build());
                        }).collect(Collectors.toList());
    }

    public List<DRegData> getCurrentDRegDetailByTxg(TxgProfile txg) throws WebException {
        LocalDateTime now = LocalDateTime.now();
        TypedPair<LocalDateTime> dateTimeRange = buildDRegDateRange(now);

        log.info("query datetime from {} to {}", dateTimeRange.left(), dateTimeRange.right());

        return getDRegDetailByTxgAndDateRange(txg, dateTimeRange);
    }
}
