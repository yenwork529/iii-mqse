package org.iii.esd.mongo.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.iii.esd.enums.DataType;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.enums.NoticeType;
import org.iii.esd.enums.OperatorStatus;
import org.iii.esd.enums.StatisticsType;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.SpinReserveBid;
import org.iii.esd.mongo.document.SpinReserveData;
import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.mongo.document.SpinReserveStatistics;
import org.iii.esd.mongo.document.TradeGroupProfile;
import org.iii.esd.mongo.repository.SpinReserveBidRepository;
import org.iii.esd.mongo.repository.SpinReserveDataRepository;
import org.iii.esd.mongo.repository.SpinReserveProfileRepository;
import org.iii.esd.mongo.repository.SpinReserveStatisticsRepository;
import org.iii.esd.utils.DatetimeUtils;
import org.iii.esd.utils.GeneralPair;
import org.iii.esd.utils.PredicateUtils;

@Service
@Log4j2
public class SpinReserveService {

    @Autowired
    private SpinReserveProfileRepository spProfileRepo;

    @Autowired
    private SpinReserveBidRepository spinReserveBidRepo;

    @Autowired
    private SpinReserveDataRepository spinReserveDataRepo;

    @Autowired
    private SpinReserveStatisticsRepository spinReserveStatisticsRepo;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private UpdateService updateService;

    @Autowired
    private FieldProfileService fieldProfileService;

    private int defaultScale = 3;

    public long addSpinReserveProfile(SpinReserveProfile srProfile) {
        srProfile.setId(updateService.genSeq(SpinReserveProfile.class));
        srProfile.setCreateTime(new Date());
        spProfileRepo.insert(srProfile);
        return srProfile.getId();
    }

    public GeneralPair<SpinReserveProfile, TradeGroupProfile> updateSpinReserveAndTradeGroup(SpinReserveProfile srProfile) {
        return null;
    }

    public SpinReserveProfile updateSpinReserveProfile(SpinReserveProfile srProfile) {
        return spProfileRepo.save(srProfile);
    }

    public Optional<SpinReserveProfile> findSpinReserveProfile(Long id) {
        return spProfileRepo.findById(id);
    }

    public SpinReserveProfile findSpinReserveProfileFirst(Long id) {
        Optional<SpinReserveProfile> sp = spProfileRepo.findById(id);
        if (!sp.isPresent()) {return null;}
        return sp.get();
    }

    public List<SpinReserveProfile> findSpinReserveProfileBySrIdSet(Set<Long> ids) {
        return spProfileRepo.findByIdIn(ids);
    }

    public List<SpinReserveProfile> findAllSpinReserveProfile() {
        return spProfileRepo.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public List<SpinReserveProfile> findEnableSpinReserveProfile() {
        return spProfileRepo.findByEnableStatusNotOrderById(EnableStatus.disable);
    }

    public List<SpinReserveProfile> findSpinReserveProfileByCompanyIdAndEnableStatus(Long companyId, EnableStatus enableStatus) {
        return findSpinReserveProfileByExample(new SpinReserveProfile(companyId, enableStatus));
    }

    public List<SpinReserveProfile> findSpinReserveProfileByExample(SpinReserveProfile spinReserveProfile) {
        return spProfileRepo.findAll(Example.of(spinReserveProfile), Sort.by(Sort.Direction.ASC, "id"));
    }

    public void deleteSpinReserveProfile(Long id) {
        if (id != null) {
            spProfileRepo.deleteById(id);
        }
    }

    public void addOrUpdateAll(Long srId, List<SpinReserveBid> spinReserveBidList) {
        for (SpinReserveBid spinReserveBid : spinReserveBidList) {
            spinReserveBid.setSpinReserveProfile(new SpinReserveProfile(srId));
            Optional<SpinReserveBid> existingOne = findOneBySrIdAndTime(srId, spinReserveBid.getTimestamp());
            if (existingOne.isPresent()) {
                spinReserveBid.setCreateTime(existingOne.get().getCreateTime());
                spinReserveBid.setId(existingOne.get().getId());
            }
        }
        spinReserveBidRepo.saveAll(spinReserveBidList);
    }

    public Optional<SpinReserveBid> findOneBySrIdAndTime(Long id, Date timestamp) {
        return spinReserveBidRepo
                .findOne(Example.of(SpinReserveBid.builder().spinReserveProfile(new SpinReserveProfile(id)).timestamp(timestamp).build()));
    }

    public List<SpinReserveBid> findAll() {
        return spinReserveBidRepo.findAll();
    }

    public List<SpinReserveBid> findAllBySrIdAndTime(Long id, Date start) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        calendar.add(Calendar.DATE, 1);
        Date end = calendar.getTime();
        return spinReserveBidRepo.findBySrIdAndTime(id, start, end);
    }

    public List<SpinReserveBid> findAllBySrIdAndTime(Long id, Date start, Date end) {
        log.info("find sr bid from {} to {}", start, end);
        List<SpinReserveBid> result = spinReserveBidRepo.findBySrIdAndTime(id, start, end);
        // log.info("result {}", result);

        return result;
    }


    public Optional<SpinReserveData> findSpinReserveData(Long srId, NoticeType noticeType, Date noticeTime) {
        return spinReserveDataRepo.findOne(Example.of(SpinReserveData.builder().
                                                                     spinReserveProfile(new SpinReserveProfile(srId != null ? srId : 0)).
                                                                     noticeType(noticeType).
                                                                     noticeTime(noticeTime).
                                                                     build()));
    }

    public List<SpinReserveData> findSpinReserveDataBySrIdAndNoticeTime(Long srId, Date start, Date end) {
        return spinReserveDataRepo.findBySrIdAndNoticeTime(srId, start, end);
    }

    public List<SpinReserveData> findSpinReserveDataBySrIdAndNoticeTypeAndNoticeTime(Long srId, NoticeType noticeType, Date start,
            Date end) {
        return spinReserveDataRepo.findBySrIdAndNoticeTypeAndNoticeTime(srId, noticeType, start, end);
    }

    public List<SpinReserveData> findSpinReserveDataBySrIdAndNoticeTypeAndStartTime(Long srId, NoticeType noticeType, Date start,
            Date end) {
        return spinReserveDataRepo.findBySrIdAndNoticeTypeAndStartTime(srId, noticeType, start, end);
    }

    public SpinReserveData saveSpinReserveData(SpinReserveData spinReserveData) {
        try {
            return spinReserveDataRepo.save(spinReserveData);
        } catch (DuplicateKeyException e) {
            spinReserveData.setId(getUniqueId(spinReserveData));
            return spinReserveDataRepo.save(spinReserveData);
        }
    }

    public List<SpinReserveData> saveSpinReserveData(List<SpinReserveData> list) {
        try {
            return spinReserveDataRepo.saveAll(list);
        } catch (DuplicateKeyException e) {
            return spinReserveDataRepo.saveAll(list.stream().map(srd -> {
                srd.setId(getUniqueId(srd));
                return srd;
            }).collect(Collectors.toList()));
        }
    }

    public Optional<SpinReserveStatistics> findSpinReserveStatistics(Long srId, StatisticsType statisticsType, Date time) {
        return spinReserveStatisticsRepo.findOne(Example.of(SpinReserveStatistics.builder().
                                                                                 spinReserveProfile(
                                                                                         new SpinReserveProfile(srId != null ? srId : 0)).
                                                                                 statisticsType(statisticsType).
                                                                                 time(time).
                                                                                 build()));
    }

    public List<SpinReserveStatistics> findSpinReserveStatisticsBySrIdAndTime(Long srId, Date start, Date end) {
        return spinReserveStatisticsRepo.findBySrIdAndTime(srId, start, end);
    }

    public List<SpinReserveStatistics> findSpinReserveStatisticsBySrIdAndStatisticsTypeAndTime(Long srId, StatisticsType statisticsType,
            Date start, Date end) {
        return spinReserveStatisticsRepo.findBySrIdAndStatisticsTypeAndTime(srId, statisticsType, start, end);
    }

    public SpinReserveStatistics saveSpinReserveStatistics(SpinReserveStatistics spinReserveStatistics) {
        try {
            return spinReserveStatisticsRepo.save(spinReserveStatistics);
        } catch (DuplicateKeyException e) {
            spinReserveStatistics.setId(getUniqueId(spinReserveStatistics));
            return spinReserveStatisticsRepo.save(spinReserveStatistics);
        }
    }

    public List<SpinReserveStatistics> saveSpinReserveStatistics(List<SpinReserveStatistics> list) {
        try {
            return spinReserveStatisticsRepo.saveAll(list);
        } catch (DuplicateKeyException e) {
            return spinReserveStatisticsRepo.saveAll(list.stream().map(srs -> {
                srs.setId(getUniqueId(srs));
                return srs;
            }).collect(Collectors.toList()));
        }
    }

    /**
     * 更新棄標註記
     *
     * @param srId
     * @param time
     */
    public Boolean updateQuitBySrIdAndTime(Long srId, Date time) {
        // 超過22:30 不用做棄標
        if (LocalTime.fromDateFields(time).compareTo(new LocalTime(22, 30)) < 0) {
            Date start = DatetimeUtils.truncated(DatetimeUtils.add(time, Calendar.MINUTE, 90), Calendar.HOUR);
            Date end = DatetimeUtils.getLastTimeOfDay(time);
            return updateService.updateColumn1ByColumn2("operatorStatus", OperatorStatus.QUIT, "id",
                    findAllBySrIdAndTime(srId, start, end).stream().map(srBid -> srBid.getId()).collect(Collectors.toSet()),
                    SpinReserveBid.class, true);
        } else {
            return false;
        }
    }

    /**
     * 依據得標容量計算即時備轉各場域降載配比
     *
     * @param srId
     * @param time
     */
    public Map<Long, Double> calculateFieldSpinReserveBidRatioBySridAndTime(Long srId, Date time) {
        Map<Long, Double> map;
        Optional<SpinReserveBid> spinReserveBid = findOneBySrIdAndTime(srId, time);

        if (spinReserveBid.isPresent()) {
            BigDecimal awarded_capacity = spinReserveBid.get().getAwarded_capacity();
            BigDecimal sr_capacity = spinReserveBid.get().getSr_capacity();

            if (awarded_capacity != null && awarded_capacity.compareTo(BigDecimal.ZERO) > 0) {
                map = buildRatioFromAwardedCapacity(awarded_capacity, spinReserveBid.get());
            } else if (sr_capacity != null && sr_capacity.compareTo(BigDecimal.ZERO) > 0) {
                map = buildRatioFromSrCapacity(sr_capacity, spinReserveBid.get());
            } else {
                map = calculateFieldNativeRatio(srId);
            }
        } else {
            map = calculateFieldNativeRatio(srId);
        }

        return map;
    }

    public void runAbandon(Long srId, Instant from, Instant to) {
        Date begin = DatetimeUtils.toDate(from);
        Date beginHour = DatetimeUtils.toDate(from.truncatedTo(ChronoUnit.HOURS));
        Date end = DatetimeUtils.toDate(to);

        List<SpinReserveBid> bidList = findAllBySrIdAndTime(srId, beginHour, end);
        List<SpinReserveBid> abandoned = bidList.stream()
                                                .filter(PredicateUtils.isExists(SpinReserveBid::getAwarded_capacity))
                                                .filter(PredicateUtils.isGreaterThanZero(SpinReserveBid::getAwarded_capacity))
                                                .sorted(Comparator.comparing(SpinReserveBid::getTimestamp))
                                                .peek(bid -> bid.setAbandon(true))
                                                .collect(Collectors.toList());

        abandoned.stream()
                 .findFirst()
                 .ifPresent(first -> first.setAbandonFrom(begin));

        spinReserveBidRepo.saveAll(abandoned);
    }

    public Optional<SpinReserveData> findSpinReserveDataBySrIdAndNoticeTypeInCurrentTime(long srId, NoticeType noticeType,
            Date currentTime) {
        return spinReserveDataRepo.findSpinReserveDataBySrIdAndNoticeTypeInCurrentTime(srId, noticeType, currentTime);
    }

    private Map<Long, Double> buildRatioFromSrCapacity(BigDecimal sr_capacity, SpinReserveBid spinReserveBid) {
        return spinReserveBid.getList()
                             .stream()
                             .collect(Collectors.groupingBy(
                                     e -> e.getFieldProfile().getId(),
                                     Collectors.averagingDouble(
                                             d -> d.getSr_capacity()
                                                   .divide(sr_capacity, defaultScale, BigDecimal.ROUND_HALF_UP)
                                                   .doubleValue())));
    }

    private Map<Long, Double> buildRatioFromAwardedCapacity(BigDecimal awarded_capacity, SpinReserveBid spinReserveBid) {
        return spinReserveBid.getList()
                             .stream()
                             .collect(Collectors.groupingBy(
                                     e -> e.getFieldProfile().getId(),
                                     Collectors.averagingDouble(
                                             d -> d.getAwarded_capacity()
                                                   .divide(awarded_capacity, defaultScale, BigDecimal.ROUND_HALF_UP)
                                                   .doubleValue())));
    }

    private Map<Long, Double> calculateFieldNativeRatio(Long srId) {
        List<FieldProfile> fields = fieldProfileService.findFieldProfileBySrId(srId, EnableStatus.enable);

        BigDecimal capacityAccu = BigDecimal.ZERO;
        Map<Long, Double> ratioMap = new HashMap<>();
        for (FieldProfile field : fields) {
            if (Objects.isNull(field.getOyod()) || field.getOyod() == 0) {
                double ratio = 1.0 / fields.size();
                return fields.stream()
                             .collect(Collectors.toMap(
                                     FieldProfile::getId, fieldProfile -> ratio));
            }

            capacityAccu = capacityAccu.add(BigDecimal.valueOf(field.getOyod()));
            ratioMap.put(field.getId(), field.getOyod().doubleValue());
        }

        final BigDecimal capacitySum = BigDecimal.valueOf(capacityAccu.longValue());

        return ratioMap.entrySet()
                       .stream()
                       .collect(Collectors.toMap(
                               Map.Entry::getKey,
                               e -> BigDecimal.valueOf(e.getValue())
                                              .divide(capacitySum, BigDecimal.ROUND_HALF_UP)
                                              .doubleValue()));
    }

    /**
     * 依據得標容量計算即時備轉各場域降載配比
     *
     * @param srId
     * @param time
     */
    public Map<Long, Double> _calculateFieldSpinReserveBidRatioBySridAndTime(Long srId, Date time) {
        Map<Long, Double> map = new HashMap<>();
        Optional<SpinReserveBid> spinReserveBid = findOneBySrIdAndTime(srId, time);
        if (spinReserveBid.isPresent()) {
            BigDecimal awarded_capacity = spinReserveBid.get().getAwarded_capacity();
            if (awarded_capacity != null) {
                map = spinReserveBid.get().getList().stream().collect(
                        Collectors.groupingBy(
                                e -> e.getFieldProfile().getId(),
                                Collectors.averagingDouble(
                                        d -> d.getAwarded_capacity().divide(awarded_capacity, defaultScale, BigDecimal.ROUND_HALF_UP)
                                              .doubleValue())
                        )
                );
            }
        }
        return map;
    }

    /**
     * 計算即時備轉基準線(前五分鐘平均用電)
     *
     * @param noticeTime
     * @param fieldId
     */
    public BigDecimal calculateBaseLine(Date noticeTime, Long... fieldId) {
        if (fieldId.length > 0) {
            List<ElectricData> edList = statisticsService.findElectricDataByFieldIdAndDataTypeAndTime(
                    new HashSet<>(Arrays.asList(fieldId)), DataType.T99,
                    DatetimeUtils.add(noticeTime, Calendar.MINUTE, -5), noticeTime);

            //			log.debug("noticeTime:{}",Constants.TIMESTAMP_FORMAT.format(noticeTime));
            //			for (ElectricData electricData : edList) {
            //				log.info("time:{}, activepower:{}",Constants.TIMESTAMP_FORMAT.format(electricData.getTime()),electricData.getActivePower());
            //			}

            // 用 kWh
            //			Double value = edList.stream()
            //					.collect(Collectors.groupingBy(ElectricData::getTime,
            //							Collectors.reducing(new ElectricData(), ElectricData::sum)))
            //					.values().stream().map(ed -> ed.getM1kW())
            //					.collect(Collectors.averagingDouble(d -> d.doubleValue()));

            // 用 activePower
            Double value = edList.stream()
                                 .collect(Collectors.groupingBy(ElectricData::getTime,
                                         Collectors.reducing(new ElectricData(), ElectricData::sum)))
                                 .values().stream().map(ed -> ed.getActivePower())
                                 .collect(Collectors.averagingDouble(d -> d.doubleValue()));

            return value != null ? new BigDecimal(value.toString()).setScale(defaultScale, BigDecimal.ROUND_HALF_UP) : null;
        } else {
            return null;
        }
    }

    private String getUniqueId(SpinReserveData spinReserveData) {
        Optional<SpinReserveData> srd = findSpinReserveData(spinReserveData.getSpinReserveProfile().getId(),
                spinReserveData.getNoticeType(), spinReserveData.getNoticeTime());
        return srd.isPresent() ? srd.get().getId() : null;
    }

    private String getUniqueId(SpinReserveStatistics spinReserveStatistics) {
        Optional<SpinReserveStatistics> srd = findSpinReserveStatistics(
                spinReserveStatistics.getSpinReserveProfile().getId(), spinReserveStatistics.getStatisticsType(),
                spinReserveStatistics.getTime());
        return srd.isPresent() ? srd.get().getId() : null;
    }

}