package org.iii.esd.server.services;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.iii.esd.enums.NoticeType;
import org.iii.esd.exception.EnumInitException;
import org.iii.esd.exception.NoDataException;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.BidResData;
import org.iii.esd.mongo.document.integrate.BidTxgData;
import org.iii.esd.mongo.document.integrate.BidTxgInfo;
import org.iii.esd.mongo.document.integrate.TxgDispatchEvent;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.repository.integrate.TxgDispatchEventRepository;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.server.domain.trial.ActionType;
import org.iii.esd.server.domain.trial.AlertType;
import org.iii.esd.server.domain.trial.DispatchEvent;
import org.iii.esd.server.domain.trial.EventType;
import org.iii.esd.server.domain.trial.NotifyType;
import org.iii.esd.server.domain.trial.ReactType;
import org.iii.esd.server.domain.trial.ServiceState;
import org.iii.esd.utils.DatetimeUtils;
import org.iii.esd.utils.GeneralPair;
import org.iii.esd.utils.TypedPair;

import static org.iii.esd.utils.DatetimeUtils.toDate;
import static org.iii.esd.utils.DatetimeUtils.toLocalDateTime;

@Service
@Log4j2
public class NewTrialDispatchService {

    private static final int INIT_HOUR = -1;
    private static final int LAST_HOUR = 24;

    @Autowired
    private TxgDispatchEventRepository txgDispatchEventRepository;
    @Autowired
    private IntegrateRelationService integrateRelationService;
    @Autowired
    private TxgFieldService txgFieldService;
    @Autowired
    private IntegrateBidService integrateBidService;

    public List<GeneralPair<Integer, BigDecimal>> buildAwardedValuesNew(List<BidTxgInfo> spinReserveBidList) {
        return spinReserveBidList.stream()
                                 .sorted(Comparator.comparing(BidTxgInfo::getTimestamp))
                                 .map(bid -> GeneralPair.construct(
                                         toLocalDateTime(bid.getTimestamp()).get(ChronoField.HOUR_OF_DAY),
                                         getAwardedValue(bid)))
                                 .collect(Collectors.toList());
    }

    private BigDecimal getAwardedValue(BidTxgInfo bid) {
        if (Objects.isNull(bid.getAwardedCapacity())) {
            return bid.getList()
                      .stream()
                      .map(detail -> Objects.isNull(detail.getAwardedCapacity()) ?
                              BigDecimal.ZERO : detail.getAwardedCapacity())
                      .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            return bid.getAwardedCapacity();
        }
    }

    public List<GeneralPair<Integer, BigDecimal>> buildFieldAwardedValuesNew(
            List<BidTxgInfo> txgInfoList, TxgFieldProfile field) {
        return txgInfoList.stream()
                          .sorted(Comparator.comparing(BidTxgInfo::getTimestamp))
                          .map(bid -> GeneralPair.construct(
                                  toLocalDateTime(bid.getTimestamp()).get(ChronoField.HOUR_OF_DAY),
                                  getAwardedValue(bid, field)))
                          .collect(Collectors.toList());
    }

    private BigDecimal getAwardedValue(BidTxgInfo bid, TxgFieldProfile field) {
        return bid.getList()
                  .stream()
                  .map(detail -> Objects.equals(detail.getResId(), field.getResId())
                          && !Objects.isNull(detail.getAwardedCapacity()) ?
                          detail.getAwardedCapacity() : BigDecimal.ZERO)
                  .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<DispatchEvent> getAbandonEventsByDate(String txgId, LocalDate date) {
        LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.of(0, 0, 0));
        LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.of(0, 0, 0)).plusDays(1).minus(1, ChronoUnit.MILLIS);

        return txgDispatchEventRepository.findAbandonByTxgIdAndDay(txgId, toDate(dayStart), toDate(dayEnd))
                                         .stream()
                                         .map(this::buildEventFromEntity)
                                         .collect(Collectors.toList());
    }

    private DispatchEvent buildEventFromEntity(TxgDispatchEvent entity) {
        try {
            return DispatchEvent.builder()
                                .eventType(EventType.ofName(entity.getEventType()))
                                .entityId(entity.getId())
                                .eventTime(entity.getCreateTime().toInstant())
                                .updateTime(entity.getUpdateTime().toInstant())
                                .actionType(ActionType.ofName(entity.getActionType()))
                                .alertType(AlertType.ofName(entity.getAlertType()))
                                .eventParam(buildEventParam(entity.getEventParams()))
                                .eventReact(buildEventReacts(entity.getEventReact(), entity.getEventType()))
                                // 20220304, 移除 Notify 建立機制，以避免 START、STOP 的錯誤發生
                                // .eventNotify(buildEventNotify(entity.getEventNotify(), entity.getEventType()))
                                .serviceState(ServiceState.ofName(entity.getServiceState()))
                                .build();
        } catch (EnumInitException ex) {
            log.error("Enum init error at entity id: {}", entity.getId());
            log.error(ExceptionUtils.getStackTrace(ex));
            throw ex;
        }
    }

    private GeneralPair<NotifyType, Instant> buildEventNotify(TxgDispatchEvent.EventNotify entity, String eventType) {
        if (Objects.isNull(entity)) {
            return null;
        } else {
            return GeneralPair.construct(
                    NotifyType.byNotify(entity.getNotifyType(), eventType, entity.getNotifyText()),
                    entity.getNotifyTime().toInstant());
        }
    }

    private GeneralPair<ReactType, Instant> buildEventReacts(TxgDispatchEvent.EventReact entity, String eventType) {
        if (Objects.isNull(entity)) {
            return null;
        } else {
            return GeneralPair.construct(
                    ReactType.byReact(entity.getReactType(), eventType, entity.getReactText()),
                    entity.getResponseTime().toInstant());
        }
    }

    private DispatchEvent.EventParam buildEventParam(TxgDispatchEvent.EventParam entity) {
        if (Objects.isNull(entity)) {
            return null;
        } else {
            return DispatchEvent.EventParam.builder()
                                           .beginTime(!Objects.isNull(entity.getBeginTime()) ?
                                                   entity.getBeginTime().toInstant() : null)
                                           .startTime(!Objects.isNull(entity.getStartTime()) ?
                                                   entity.getStartTime().toInstant() : null)
                                           .endTime(!Objects.isNull(entity.getEndTime()) ?
                                                   entity.getEndTime().toInstant() : null)
                                           .capacity(!Objects.isNull(entity.getCapacity()) ?
                                                   entity.getCapacity() : null)
                                           .stopTime(!Objects.isNull(entity.getStopTime()) ?
                                                   entity.getStopTime().toInstant() : null)
                                           .startStandByTime(!Objects.isNull(entity.getStartStandByTime()) ?
                                                   entity.getStartStandByTime().toInstant() : null)
                                           .stopStandByTime(!Objects.isNull(entity.getStopStandByTime()) ?
                                                   entity.getStopStandByTime().toInstant() : null)
                                           .startServiceTime(!Objects.isNull(entity.getStartServiceTime()) ?
                                                   entity.getStartServiceTime().toInstant() : null)
                                           .stopServiceTime(!Objects.isNull(entity.getStopServiceTime()) ?
                                                   entity.getStopServiceTime().toInstant() : null)
                                           .abandonFromTime(!Objects.isNull(entity.getAbandonFromTime()) ?
                                                   entity.getAbandonFromTime().toInstant() : null)
                                           .abandonToTime(!Objects.isNull(entity.getAbandonToTime()) ?
                                                   entity.getAbandonToTime().toInstant() : null)
                                           .state(!Objects.isNull(entity.getState()) ?
                                                   entity.getState() : null)
                                           .build();
        }
    }

    public List<DispatchEvent> getEventsByDate(String txgId, LocalDate date) {
        Date start = DatetimeUtils.toDate(date.atTime(0, 0, 0));
        Date end = DatetimeUtils.toDate(date.atTime(23, 59, 59));

        List<DispatchEvent> typeA = findTypeADispatchEventByTxgIdAndDate(txgId, start, end);
        List<DispatchEvent> typeB = findTypeBDispatchEventByTxgIdAndDate(txgId, start, end);
        List<DispatchEvent> typeC = findTypeCDispatchEventByTxgIdAndDate(txgId, start, end);

        return Stream.of(typeA, typeB, typeC)
                     .flatMap(Collection::stream)
                     .collect(Collectors.toList());
    }

    private List<DispatchEvent> findTypeADispatchEventByTxgIdAndDate(String txgId, Date start, Date end) {
        List<TxgDispatchEvent> typeABegin =
                txgDispatchEventRepository.findTypeABeginByTxgIdAndDateRange(txgId, start, end);

        List<TxgDispatchEvent> typeARunning =
                txgDispatchEventRepository.findTypeARunningByTxgIdAndDateRange(txgId, start, end);

        List<TxgDispatchEvent> typeAEnd =
                txgDispatchEventRepository.findTypeAEndByTxgIdAndDateRange(txgId, start, end);

        List<TxgDispatchEvent> typeADone =
                txgDispatchEventRepository.findTypeADoneByTxgIdAndDateRange(txgId, start, end);

        return Stream.of(typeABegin, typeARunning, typeAEnd, typeADone)
                     .flatMap(Collection::stream)
                     .map(this::buildEventFromEntity)
                     .collect(Collectors.toList());
    }

    private List<DispatchEvent> findTypeBDispatchEventByTxgIdAndDate(String txgId, Date start, Date end) {
        List<TxgDispatchEvent> typeB =
                txgDispatchEventRepository.findTypeBByTxgIdAndDateRange(txgId, start, end);

        return typeB.stream()
                    .map(this::buildEventFromEntity)
                    .collect(Collectors.toList());
    }

    private List<DispatchEvent> findTypeCDispatchEventByTxgIdAndDate(String txgId, Date start, Date end) {
        List<TxgDispatchEvent> typeCStandBy =
                txgDispatchEventRepository.findTypeCStandByByTxgIdAndDateRange(txgId, start, end);

        List<TxgDispatchEvent> typeCService =
                txgDispatchEventRepository.findTypeCServiceByTxgIdAndDateRange(txgId, start, end);

        List<TxgDispatchEvent> typeCAbandon =
                txgDispatchEventRepository.findAbandonByTxgIdAndDay(txgId, start, end);

        return Stream.of(typeCStandBy, typeCService, typeCAbandon)
                     .flatMap(Collection::stream)
                     .map(this::buildEventFromEntity)
                     .collect(Collectors.toList());
    }

    public List<DispatchEvent> getFieldEventsByDate(String resId, LocalDate localDate) throws WebException {
        TxgFieldProfile field = txgFieldService.getByResId(resId);
        TxgProfile txg = integrateRelationService.seekTxgProfileFromTxgId(field.getTxgId());

        Date start = DatetimeUtils.toDate(localDate.atTime(0, 0, 0));
        Date end = DatetimeUtils.toDate(localDate.atTime(23, 59, 59));

        // List<SpinReserveData> srData = srService.findSpinReserveDataBySrIdAndNoticeTime(sr.getId(), begin, end);
        List<BidTxgData> srData = integrateBidService.findDataByTxgIdAndNoticeTime(txg.getTxgId(), start, end);
        log.info("sr data: {}", srData);

        try {
            List<DispatchEvent> typeA = buildFieldTypeAByLegacyDataNew(field, srData);
            List<DispatchEvent> typeB = buildFieldTypeBByLegacyDataNew(srData);

            // List<SpinReserveBid> srBid = srService.findAllBySrIdAndTime(sr.getId(), begin, end);
            List<BidTxgInfo> srBid = integrateBidService.findInfoByTxgIdAndTime(txg.getTxgId(), start, end);
            List<DispatchEvent> typeC = buildFieldTypeCByLegacyBidNew(srBid);

            return Stream.of(typeA, typeB, typeC)
                         .flatMap(Collection::stream)
                         .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return Collections.emptyList();
        }
    }

    private static final String TEMPLATE_LEGACY_DATA_NO_RES = "BidResData has no such res id %s";

    private List<DispatchEvent> buildFieldTypeAByLegacyDataNew(TxgFieldProfile field, List<BidTxgData> srData) {
        return srData.stream()
                     .filter(data -> NoticeType.UNLOAD == data.getNoticeType())
                     .map(data -> {

                         String resId = field.getResId();

                         BidResData fieldData =
                                 data.getList()
                                     .stream()
                                     .filter(detail -> Objects.equals(resId, detail.getResId()))
                                     .findFirst()
                                     .orElseThrow(() ->
                                             new NoDataException(resId, TEMPLATE_LEGACY_DATA_NO_RES));

                         return GeneralPair.construct(data, fieldData);
                     })
                     .flatMap(this::buildFieldTypeANew)
                     .collect(Collectors.toList());
    }

    private List<DispatchEvent> buildFieldTypeBByLegacyDataNew(List<BidTxgData> srData) {
        return srData.stream()
                     .filter(data -> NoticeType.ALERT == data.getNoticeType())
                     .map(data -> buildTypeB(data.getNoticeTime().toInstant()))
                     .collect(Collectors.toList());
    }

    private List<DispatchEvent> buildFieldTypeCByLegacyBidNew(List<BidTxgInfo> srBid) {
        return buildTypeCEventsNew(srBid);
    }

    private Stream<DispatchEvent> buildFieldTypeANew(GeneralPair<BidTxgData, BidResData> dataPair) {
        BidTxgData srData = dataPair.left();
        BidResData fieldData = dataPair.right();

        Instant begin = srData.getNoticeTime().toInstant();
        Instant start = srData.getStartTime().toInstant();
        Instant stop = srData.getEndTime().toInstant();
        Long capacity = fieldData.getClipKW().longValue();

        DispatchEvent typeABegin = buildTypeABegin(begin, start, stop, capacity);
        DispatchEvent typeARunning = buildTypeARunning(begin, start, capacity);
        DispatchEvent typeADone = buildTypeADone(begin, start, stop);

        return Stream.of(typeABegin, typeARunning, typeADone);
    }

    private DispatchEvent buildTypeB(Instant noticeTime) {
        return DispatchEvent.builder()
                            .eventType(EventType.TYPE_B)
                            .alertType(AlertType.CONSUME_NOT_ENOUGH)
                            .eventTime(noticeTime)
                            .build();
    }

    private List<DispatchEvent> buildTypeCEventsNew(List<BidTxgInfo> txgInfoList) {
        if (CollectionUtils.isEmpty(txgInfoList)) {
            return Collections.emptyList();
        }

        // 推導出每個時段是否有得標
        List<GeneralPair<Integer, Boolean>> awardedStates = buildAwardedStatesNew(txgInfoList);

        // log.info("awarded states: {}", awardedStates);

        // 過濾出有得標的區段
        List<TypedPair<Integer>> awardedRanges = buildAwardedRanges(awardedStates);

        // 建出 TypeC 物件
        Instant bidDay = txgInfoList.get(0)
                                    .getTimestamp()
                                    .toInstant();
        Instant dayStart = DatetimeUtils.getStartOfLocal(bidDay);

        return awardedRanges.stream()
                            .flatMap(range -> this.buildTypeC(dayStart, range)
                                                  .toList()
                                                  .stream())
                            .collect(Collectors.toList());
    }

    private DispatchEvent buildTypeABegin(Instant beginTime, Instant startTime, Instant stopTime, Long capacity) {
        return DispatchEvent.builder()
                            .eventType(EventType.TYPE_A)
                            .actionType(ActionType.BEGIN)
                            .eventTime(beginTime)
                            .eventParam(DispatchEvent.EventParam.builder()
                                                                .beginTime(beginTime)
                                                                .startTime(startTime)
                                                                .stopTime(stopTime)
                                                                .capacity(capacity)
                                                                .build())
                            .build();
    }

    private DispatchEvent buildTypeARunning(Instant beginTime, Instant startTime, Long capacity) {
        return DispatchEvent.builder()
                            .eventType(EventType.TYPE_A)
                            .actionType(ActionType.RUNNING)
                            .eventTime(beginTime)
                            .eventParam(DispatchEvent.EventParam.builder()
                                                                .startTime(startTime)
                                                                .capacity(capacity)
                                                                .build())
                            .build();
    }

    private DispatchEvent buildTypeADone(Instant beginTime, Instant startTime, Instant stopTime) {
        return DispatchEvent.builder()
                            .eventType(EventType.TYPE_A)
                            .actionType(ActionType.DONE)
                            .eventTime(beginTime)
                            .eventParam(DispatchEvent.EventParam.builder()
                                                                .startTime(startTime)
                                                                .stopTime(stopTime)
                                                                .build())
                            .build();
    }

    private List<GeneralPair<Integer, Boolean>> buildAwardedStatesNew(List<BidTxgInfo> txgInfoList) {
        return txgInfoList.stream()
                          .sorted(Comparator.comparing(BidTxgInfo::getTimestamp))
                          .map(bid -> GeneralPair.construct(
                                  toLocalDateTime(bid.getTimestamp()).get(ChronoField.HOUR_OF_DAY),
                                  isAwarded(bid)))
                          .collect(Collectors.toList());
    }

    private List<TypedPair<Integer>> buildAwardedRanges(List<GeneralPair<Integer, Boolean>> awardedStates) {
        List<TypedPair<Integer>> typeCPairs = new LinkedList<>();

        TypedPair<Integer> typeCPair = initTypeCPair();
        for (GeneralPair<Integer, Boolean> awardedState : awardedStates) {
            int hour = awardedState.left();
            boolean awarded = awardedState.right();

            if (awarded) {
                if (isNotStartYet(typeCPair)) {
                    typeCPair = new TypedPair<>(hour, INIT_HOUR);
                }
            } else {
                if (isNotEndYet(typeCPair)) {
                    typeCPair = new TypedPair<>(typeCPair.left(), hour);
                }
            }

            if (isFufilled(typeCPair)) {
                typeCPairs.add(typeCPair);
                typeCPair = initTypeCPair();
            }
        }

        if (isNotEndYet(typeCPair)) {
            typeCPair = TypedPair.cons(typeCPair.left(), LAST_HOUR);
            typeCPairs.add(typeCPair);
        }

        return typeCPairs;
    }

    private TypedPair<DispatchEvent> buildTypeC(Instant tomorrowStart, TypedPair<Integer> range) {
        Instant now = Instant.now();

        int startHour = range.left();
        Instant startTime = tomorrowStart.plus(startHour, ChronoUnit.HOURS);
        DispatchEvent startEvent = DispatchEvent.builder()
                                                .eventType(EventType.TYPE_C)
                                                .serviceState(ServiceState.START)
                                                .eventParam(DispatchEvent.EventParam.builder()
                                                                                    .startStandByTime(startTime)
                                                                                    .build())
                                                .eventTime(now)
                                                .build();

        int stopHour = range.right();
        Instant stopTime = tomorrowStart.plus(stopHour, ChronoUnit.HOURS)
                                        .minusSeconds(1);
        DispatchEvent stopEvent = DispatchEvent.builder()
                                               .eventType(EventType.TYPE_C)
                                               .serviceState(ServiceState.STOP)
                                               .eventParam(DispatchEvent.EventParam.builder()
                                                                                   .stopStandByTime(stopTime)
                                                                                   .build())
                                               .eventTime(now)
                                               .build();

        return TypedPair.cons(startEvent, stopEvent);
    }

    private Boolean isAwarded(BidTxgInfo bid) {
        // log.info("check awarded: {}", bid.getAwarded_capacity());

        if (Objects.isNull(bid.getAwardedCapacity())) {
            BigDecimal fieldSum = bid.getList()
                                     .stream()
                                     .map(detail -> Objects.isNull(detail.getAwardedCapacity()) ?
                                             BigDecimal.ZERO : detail.getAwardedCapacity())
                                     .reduce(BigDecimal.ZERO, BigDecimal::add);

            return 0.0 < fieldSum.doubleValue();
        } else {
            return 0.0 < bid.getAwardedCapacity().doubleValue();
        }
    }

    private TypedPair<Integer> initTypeCPair() {
        return TypedPair.cons(INIT_HOUR, INIT_HOUR);
    }

    private boolean isNotStartYet(TypedPair<Integer> typeCPair) {
        return typeCPair.left() == INIT_HOUR;
    }

    private boolean isFufilled(TypedPair<Integer> typeCPair) {
        return typeCPair.left() != INIT_HOUR
                && typeCPair.right() != INIT_HOUR;
    }

    private boolean isNotEndYet(TypedPair<Integer> typeCPair) {
        return !isNotStartYet(typeCPair)
                && typeCPair.right() == INIT_HOUR;
    }

}
