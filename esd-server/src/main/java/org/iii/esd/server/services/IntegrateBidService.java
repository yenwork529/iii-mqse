package org.iii.esd.server.services;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.enums.NoticeType;
import org.iii.esd.enums.StatisticsType;
import org.iii.esd.mongo.dao.DrResDataDao;
import org.iii.esd.mongo.dao.DrTxgDataDao;
import org.iii.esd.mongo.document.integrate.BidResBid;
import org.iii.esd.mongo.document.integrate.BidResData;
import org.iii.esd.mongo.document.integrate.BidResInfo;
import org.iii.esd.mongo.document.integrate.BidResStatistics;
import org.iii.esd.mongo.document.integrate.BidTxgBid;
import org.iii.esd.mongo.document.integrate.BidTxgData;
import org.iii.esd.mongo.document.integrate.BidTxgInfo;
import org.iii.esd.mongo.document.integrate.BidTxgStatistics;
import org.iii.esd.mongo.document.integrate.DrResData;
import org.iii.esd.mongo.document.integrate.DrTxgData;
import org.iii.esd.mongo.document.integrate.RevenueFactor;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.repository.integrate.BidResBidRepository;
import org.iii.esd.mongo.repository.integrate.BidResDataRepository;
import org.iii.esd.mongo.repository.integrate.BidResInfoRepository;
import org.iii.esd.mongo.repository.integrate.BidTxgBidRepository;
import org.iii.esd.mongo.repository.integrate.BidTxgDataRepository;
import org.iii.esd.mongo.repository.integrate.BidTxgInfoRepository;
import org.iii.esd.mongo.repository.integrate.RevenueFactoryRepository;
import org.iii.esd.mongo.service.integrate.IntegrateDataService;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.thirdparty.service.HttpService;
import org.iii.esd.utils.DatetimeUtils;
import org.iii.esd.utils.GeneralPair;
import org.iii.esd.utils.PredicateUtils;

import static java.math.RoundingMode.HALF_UP;
import static org.iii.esd.mongo.util.ModelHelper.asNonNull;
import static org.iii.esd.utils.DatetimeUtils.toDate;

@Service
@Log4j2
public class IntegrateBidService {

    @Autowired
    private BidResBidRepository resBidRepository;
    @Autowired
    private BidResInfoRepository resInfoRepository;
    @Autowired
    private BidTxgBidRepository txgBidRepository;
    @Autowired
    private BidTxgInfoRepository txgInfoRepository;
    @Autowired
    private IntegrateRelationService relationService;
    @Autowired
    private IntegrateDataService dataService;
    @Autowired
    private BidTxgDataRepository txgDataRepository;
    @Autowired
    private BidResDataRepository resDataRepository;
    @Autowired
    private IntegrateDataService iDataService;
    @Autowired
    private MongoOperations mongoOperations;
    @Autowired
    private RevenueFactoryRepository revenueFactoryRepository;
    @Autowired
    private DrTxgDataDao drTxgDataRepository;
    @Autowired
    private DrResDataDao drResDataRepository;

    @Autowired
    HttpService httpService;

    String backend = null;

    public Optional<RevenueFactor> findRevenueFactorByOrgIdAndTimestamp(String orgId, Date timestamp) {
        String id = RevenueFactor.buildId(orgId, timestamp.getTime());
        return revenueFactoryRepository.findById(id);
    }

    public RevenueFactor recalculateTxgRevenueFactor1(TxgProfile txg, BidTxgData spinReserveData) {
        Date t1 = spinReserveData.getStartTime();
        Optional<DrTxgData> mbD1 = drTxgDataRepository.findById(txg.getTxgId() + "_" + t1.getTime());

        Date tn = spinReserveData.getEndTime();
        Optional<DrTxgData> mbDn = drTxgDataRepository.findById(txg.getTxgId() + "_" + tn.getTime());

        if (mbD1.isEmpty() || mbDn.isEmpty()) {
            return RevenueFactor.DEFAULT;
        }

        DrTxgData d1 = mbD1.get();
        BigDecimal skwh = d1.getG1M1EnergyNet();

        DrTxgData dn = mbDn.get();
        BigDecimal ekwh = dn.getG1M1EnergyNet();

        BigDecimal clip = BigDecimal.valueOf(spinReserveData.getClipKW());

        BigDecimal rf = calculateRevenueFactor1(RevenueFactorParam.builder()
                                                                  .t1(t1)
                                                                  .tn(tn)
                                                                  .clp(clip)
                                                                  .skwh(skwh)
                                                                  .ekwh(ekwh)
                                                                  .build());

        return saveTxgRevenueFactor(txg, spinReserveData, rf);
    }


    public RevenueFactor recalculateTxgRevenueFactor2(TxgProfile txg, BidTxgData txgData) {
        Date t1 = txgData.getStartTime();
        Date tn = txgData.getEndTime();
        List<DrTxgData> dataList = drTxgDataRepository.findFromTxgIdAndTimestamp(txg.getTxgId(), t1, tn);

        if (CollectionUtils.isEmpty(dataList)) {
            return RevenueFactor.DEFAULT;
        }

        BigDecimal clip = BigDecimal.valueOf(txgData.getClipKW());
        BigDecimal cbl = txgData.getBaseline();
        BigDecimal target = cbl.subtract(clip);

        BigDecimal rf = calculateRevenueFactor2(
                RevenueFactorParam.builder()
                                  .t1(t1)
                                  .tn(tn)
                                  .clp(clip)
                                  .cbl(cbl)
                                  .target(target)
                                  .kwList(dataList.stream()
                                                  .map(data -> GeneralPair.construct(data.getTimestamp(), data.getG1M1kW()))
                                                  .collect(Collectors.toList()))
                                  .build());

        return saveTxgRevenueFactor(txg, txgData, rf);
    }

    /**
     * 給定：<br/>
     * 1. CLP :: 每分鐘降載量 (kW)<br/>
     * 2. CBL :: 基準量 (kW)<br/>
     * 3. CAP (0-n) :: 調度期間每分中之功率(kW)<br/>
     * 則：<br/>
     * a. Σ (CBL - CAPx) / CLP = SumRF :: 每分鐘執行率總合<br/>
     * b. SumRF / n = AvgRF :: 平均執行率(%)<br/>
     */
    private BigDecimal calculateRevenueFactor2(RevenueFactorParam param) {
        log.info("revenue factor param: {}", param);

        BigDecimal sumRF = param.kwList.stream()
                                       .map(kWWithTime -> {
                                           BigDecimal kW = kWWithTime.right();
                                           BigDecimal dkW = param.getCbl().subtract(kW);

                                           return dkW.divide(param.getClp(), 5, HALF_UP)
                                                     .multiply(BigDecimal.valueOf(100.0));
                                       }).reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("sum rf: {}", sumRF);

        // double ttp = Long.valueOf((param.getTn().getTime() - param.getT1().getTime()) / (60 * 1000L)).doubleValue();
        // BigDecimal avgRF = sumRF.divide(BigDecimal.valueOf(ttp), 2, HALF_UP);

        BigDecimal avgRF = sumRF.divide(BigDecimal.valueOf(param.kwList.size()), 2, HALF_UP);
        log.info("avg rf: {}", avgRF);

        return avgRF;
    }

    /**
     * 給定：<br/>
     * 1. CLP :: 每分鐘降載量 (kW)<br/>
     * 2. TTP :: 總調度時間長 (分鐘)<br/>
     * 3. SKWH :: 第 1 分鐘用電量 (kWH)<br/>
     * 4. EKWH :: 最後一分鐘用電量 (kWH)<br/>
     * 則：<br/>
     * a. CLP * TTP = TDR :: 目標需量 (kWH)<br/>
     * b. EKWH - SKWH = ADR :: 實際需量 (kWH)<br/>
     * c. ADR / TDR = RF :: 降載執行率 (%)<br/>
     */
    private BigDecimal calculateRevenueFactor1(RevenueFactorParam param) {
        log.info("revenue factor param: {}", param);

        long ttp = (param.getTn().getTime() - param.getT1().getTime()) / (60 * 1000L);
        BigDecimal tdr = param.getClp().multiply(BigDecimal.valueOf(ttp));
        BigDecimal adr = param.getEkwh().subtract(param.getSkwh());
        log.info("tdr: {} and adr: {}", tdr, adr);

        BigDecimal rf = adr.divide(tdr, 4, HALF_UP)
                           .multiply(BigDecimal.valueOf(100));
        log.info("revenue factor: {}", rf);

        return rf;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString
    public static class RevenueFactorParam {
        private Date t1;
        private Date tn;
        private BigDecimal clp;
        private BigDecimal skwh;
        private BigDecimal ekwh;
        private BigDecimal cbl;
        private BigDecimal target;
        private List<GeneralPair<Date, BigDecimal>> kwList;
    }

    public RevenueFactor recalculateResRevenueFactor1(TxgFieldProfile res, BidResData resData, BidTxgData txgData) {
        Date t1 = txgData.getStartTime();
        Optional<DrResData> mbD1 = drResDataRepository.findById(res.getResId() + "_" + t1.getTime());

        Date tn = txgData.getEndTime();
        Optional<DrResData> mbDn = drResDataRepository.findById(res.getResId() + "_" + tn.getTime());

        if (mbD1.isEmpty() || mbDn.isEmpty()) {
            return RevenueFactor.DEFAULT;
        }

        DrResData d1 = mbD1.get();
        BigDecimal skwh = d1.getM1EnergyNET();

        DrResData dn = mbDn.get();
        BigDecimal ekwh = dn.getM1EnergyNET();

        BigDecimal clip = BigDecimal.valueOf(resData.getClipKW());

        BigDecimal rf = calculateRevenueFactor1(RevenueFactorParam.builder()
                                                                  .t1(t1)
                                                                  .tn(tn)
                                                                  .clp(clip)
                                                                  .skwh(skwh)
                                                                  .ekwh(ekwh)
                                                                  .build());

        return saveResRevenueFactor(res, txgData, rf);

    }

    public RevenueFactor recalculateResRevenueFactor2(TxgFieldProfile res, BidResData resData, BidTxgData txgData) {
        Date t1 = txgData.getStartTime();
        Date tn = txgData.getEndTime();
        List<DrResData> dataList = drResDataRepository.findByResIdAndTime(res.getResId(), t1, tn);

        if (CollectionUtils.isEmpty(dataList)) {
            return RevenueFactor.DEFAULT;
        }

        BigDecimal clip = BigDecimal.valueOf(resData.getClipKW());
        BigDecimal cbl = resData.getBaseline();
        BigDecimal target = cbl.subtract(clip);

        BigDecimal rf = calculateRevenueFactor2(
                RevenueFactorParam.builder()
                                  .t1(t1)
                                  .tn(tn)
                                  .clp(clip)
                                  .cbl(cbl)
                                  .target(target)
                                  .kwList(dataList.stream()
                                                  .map(data -> GeneralPair.construct(data.getTimestamp(), data.getM1kW()))
                                                  .collect(Collectors.toList()))
                                  .build());

        return saveResRevenueFactor(res, txgData, rf);

    }

    private RevenueFactor saveTxgRevenueFactor(TxgProfile txg, BidTxgData spinReserveData, BigDecimal rf) {
        RevenueFactor entity = new RevenueFactor(txg.getTxgId(), spinReserveData.getTimestamp());
        entity.setRevenueFactor(rf);

        return revenueFactoryRepository.save(entity);
    }

    private RevenueFactor saveResRevenueFactor(TxgFieldProfile res, BidTxgData spinReserveData, BigDecimal rf) {
        RevenueFactor entity = new RevenueFactor(res.getResId(), spinReserveData.getTimestamp());
        entity.setRevenueFactor(rf);

        return revenueFactoryRepository.save(entity);
    }

    @PostConstruct
    void postcons() {
        backend = System.getenv("ESD_BACKEND_URL"); // e.g. "http://172.17.0.1:8060"
        if (backend == null) {
            // backend = "http://172.17.0.1:8060";
            backend = "http://trial-app:8060";
        }
    }

    public void makeAbandon(String txgId, Date from, Date to) {
        String url = String.format(backend + "/bid/makeabandon/%s/%d/%d", txgId, from.getTime(), to.getTime());
        log.info("post bid.makeabandon>>" + url);
        ApiResponse res = httpService.jsonPost(url, url, SuccessfulResponse.class);
        log.info("returned bid.makeabandon<<{}", res);
    }

    public void updateBidInfo(String txgId, List<BidTxgInfo> bidtxginfolst, List<BidResInfo> bidresinfolst) {
        if (CollectionUtils.isNotEmpty(bidtxginfolst)) {
            iDataService.saveAllBidTxgInfo(bidtxginfolst);
            BidTxgInfo txgInfo = bidtxginfolst.get(0);

            if (CollectionUtils.isNotEmpty(bidresinfolst)) {
                iDataService.saveAllBidResInfo(bidresinfolst);
            }

            String url = String.format(backend + "/bid/updateinfo/%s/%d", txgId, txgInfo.getTimestamp().getTime());
            log.info("post bid.updateInfo>>" + url);
            ApiResponse res = httpService.jsonPost(url, url, SuccessfulResponse.class);
            log.info("returned bid.updateInfo<<{}", res);
        }
    }

    public void makeDayStatistics(String txgId, Date from) {
        String url = String.format(backend + "/statistics/%s/day/%d", txgId, from.getTime());
        log.info("post day.statistics>>" + url);
        // ApiResponse res = httpService.jsonPost(url, url);
        ApiResponse res = httpService.jsonPost(url, url, SuccessfulResponse.class);
        log.info("returned day.statistics<<{}", res);
    }

    public void makeMonthStatistics(String txgId, Date from) {
        String url = String.format(backend + "/statistics/%s/month/%d", txgId, from.getTime());
        log.info("post month.statistics>>" + url);
        // ApiResponse res = httpService.jsonPost(url, url);
        ApiResponse res = httpService.jsonPost(url, url, SuccessfulResponse.class);
        log.info("returned month.statistics<<{}", res);
    }


    public List<BidTxgInfo> findInfoByTxgIdAndTime(String txgId, Date start) {
        Date end = DatetimeUtils.plusOneDay(start);

        return findInfoByTxgIdAndTime(txgId, start, end);
    }

    public List<BidTxgInfo> findInfoByTxgIdAndTime(String txgId, Date start, Date end) {
        List<BidTxgInfo> txgInfoList = txgInfoRepository.findByTxgIdAndTime(txgId, start, end);
        List<TxgFieldProfile> resList = asNonNull(relationService.seekTxgFieldProfilesFromTxgId(txgId));

        txgInfoList.forEach(txgInfo ->
                txgInfo.setList(
                        resInfoRepository.findByResIdInAndTime(getResIds(resList), txgInfo.getTimestamp())
                ));

        return txgInfoList;
    }

    private Set<String> getResIds(List<TxgFieldProfile> resList) {
        return resList.stream()
                      .map(TxgFieldProfile::getResId)
                      .collect(Collectors.toSet());
    }

    public List<BidTxgBid> findBidByTxgIdAndTime(String txgId, Date start) {
        Date end = DatetimeUtils.plusOneDay(start);

        List<BidTxgBid> txgBidList = txgBidRepository.findByTxgIdAndTime(txgId, start, end);
        List<TxgFieldProfile> resList = asNonNull(relationService.seekTxgFieldProfilesFromTxgId(txgId));

        txgBidList.forEach(txgBid ->
                txgBid.setList(
                        resBidRepository.findByResIdInAndTime(getResIds(resList), txgBid.getTimestamp())
                ));

        return txgBidList;
    }

    public List<BidTxgData> findDataByTxgIdAndNoticeTypeAndNoticeTime(String txgId, NoticeType noticeType, Date start, Date end) {
        List<BidTxgData> txgDataList = txgDataRepository.findByTxgIdAndNoticeTypeAndNoticeTime(txgId, noticeType, start, end);
        List<TxgFieldProfile> resList = asNonNull(relationService.seekTxgFieldProfilesFromTxgId(txgId));

        txgDataList.forEach(txgData ->
                txgData.setList(
                        resDataRepository.findByResIdInAndNoticeTime(getResIds(resList), txgData.getNoticeTime())
                ));

        return txgDataList;
    }

    public List<BidTxgBid> findBidByTxgIdAndTime(String txgId, Date start, Date end) {
        List<BidTxgBid> txgBidList = txgBidRepository.findByTxgIdAndTime(txgId, start, end);
        List<TxgFieldProfile> resList = asNonNull(relationService.seekTxgFieldProfilesFromTxgId(txgId));

        txgBidList.forEach(txgInfo ->
                txgInfo.setList(
                        resBidRepository.findByResIdInAndTime(getResIds(resList), txgInfo.getTimestamp())
                ));

        return txgBidList;
    }

    public void saveInfoByTxgId(String txgId, List<BidTxgInfo> txgInfoList) {
        List<BidResInfo> resInfoList = new LinkedList<>();

        for (BidTxgInfo txgInfo : txgInfoList) {
            BidTxgInfo existingOne = txgInfoRepository.findByTxgIdAndTime(txgId, txgInfo.getTimestamp());

            if (Objects.isNull(existingOne)) {
                txgInfo.initial();
            } else {
                txgInfo.setId(existingOne.getId());
                txgInfo.setCreateTime(existingOne.getCreateTime());
                txgInfo.setTimestamp(existingOne.getTimestamp());
                txgInfo.setTimeticks(existingOne.getTimeticks());
            }

            for (BidResInfo resInfo : txgInfo.getList()) {
                BidResInfo existingOneRes = resInfoRepository.findByResIdAndTime(resInfo.getResId(), resInfo.getTimestamp());

                if (Objects.isNull(existingOneRes)) {
                    resInfo.initial();
                } else {
                    resInfo.setId(existingOneRes.getId());
                    resInfo.setCreateTime(existingOneRes.getCreateTime());
                    resInfo.setTimestamp(existingOneRes.getTimestamp());
                    resInfo.setTimeticks(existingOneRes.getTimeticks());
                }
            }

            resInfoList.addAll(txgInfo.getList());
        }

        updateBidInfo(txgId, txgInfoList, resInfoList);
        // iDataService.saveAllBidTxgInfo(txgInfoList);
        // iDataService.saveAllBidResInfo(resInfoList);
    }

    public void saveBidByTxgId(String txgId, ArrayList<BidTxgBid> txgBidList) {
        List<BidResBid> resBidList = new LinkedList<>();

        for (BidTxgBid txgBid : txgBidList) {
            BidTxgInfo existingOne = txgInfoRepository.findByTxgIdAndTime(txgId, txgBid.getTimestamp());

            if (!Objects.isNull(existingOne)) {
                // txgBid.setCreateTime(existingOne.getCreateTime());
                txgBid.setId(existingOne.getId());
            }

            for (BidResBid resBid : txgBid.getList()) {
                BidResInfo existingOneRes = resInfoRepository.findByResIdAndTime(resBid.getResId(), resBid.getTimestamp());

                if (!Objects.isNull(existingOneRes)) {
                    resBid.setCreateTime(existingOne.getCreateTime());
                    resBid.setId(existingOneRes.getId());
                }
            }

            resBidList.addAll(txgBid.getList());
        }

        txgBidRepository.saveAll(txgBidList);
        resBidRepository.saveAll(resBidList);
    }

    public Optional<BidTxgInfo> findOneByTxgIdAndTime(String txgId, Date timestamp) {
        return txgInfoRepository.findOne(Example.of(BidTxgInfo.builder()
                                                              .txgId(txgId)
                                                              .timestamp(timestamp)
                                                              .build()));
    }

    public List<BidTxgData> findDataByTxgIdAndNoticeTime(String txgId, Date start, Date end) {
        List<BidTxgData> txgDataList = txgDataRepository.findByTxgIdAndNoticeTime(txgId, start, end);
        txgDataList.forEach(txgData -> {
            List<TxgFieldProfile> resList = asNonNull(relationService.seekTxgFieldProfilesFromTxgId(txgId));
            List<BidResData> resDataList = new ArrayList<>(
                    resDataRepository.findByResIdInAndNoticeTime(resList.stream()
                                                                        .map(TxgFieldProfile::getResId)
                                                                        .collect(Collectors.toSet()), txgData.getNoticeTime()));

            txgData.setList(resDataList);
        });

        return txgDataList;
    }

    public List<BidTxgData> findUnloadDataByTxgIdAndNoticeTime(String txgId, Date start, Date end) {
        List<BidTxgData> txgDataList = txgDataRepository.findByTxgIdAndNoticeTypeAndNoticeTime(txgId, NoticeType.UNLOAD, start, end);
        txgDataList.forEach(txgData -> {
            List<TxgFieldProfile> resList = asNonNull(relationService.seekTxgFieldProfilesFromTxgId(txgId));
            List<BidResData> resDataList = new ArrayList<>(
                    resDataRepository.findByResIdInAndNoticeTime(resList.stream()
                                                                        .map(TxgFieldProfile::getResId)
                                                                        .collect(Collectors.toSet()), txgData.getNoticeTime()));

            txgData.setList(resDataList);
        });

        return txgDataList;
    }

    public void runAbandon(String txgId, Instant from, Instant to) {
        Date begin = DatetimeUtils.toDate(from);
        Date beginHour = DatetimeUtils.toDate(from.truncatedTo(ChronoUnit.HOURS));
        Date end = DatetimeUtils.toDate(to);

        List<BidTxgInfo> bidList = txgInfoRepository.findByTxgIdAndTime(txgId, beginHour, end);
        List<BidTxgInfo> abandoned = bidList.stream()
                                            .filter(PredicateUtils.isExists(BidTxgInfo::getAwardedCapacity))
                                            .filter(PredicateUtils.isGreaterThanZero(BidTxgInfo::getAwardedCapacity))
                                            .sorted(Comparator.comparing(BidTxgInfo::getTimestamp))
                                            .peek(bid -> bid.setAbandon(true))
                                            .collect(Collectors.toList());

        abandoned.stream()
                 .findFirst()
                 .ifPresent(first -> first.setAbandonFrom(begin));

        txgInfoRepository.saveAll(abandoned);

        TxgProfile txg = relationService.seekTxgProfileFromTxgId(txgId);
        makeAbandon(txg.getTxgId(), toDate(from), toDate(to));
    }

    public List<BidTxgStatistics> findHourlyStatsByTxgIdAndDateTime(String txgId, Date start, Date end) {
        List<BidTxgStatistics> txgStats = getTxgStatistics(txgId, start, end);
        if (CollectionUtils.isEmpty(txgStats)) {
            return Collections.emptyList();
        }

        List<TxgFieldProfile> resList = asNonNull(relationService.seekTxgFieldProfilesFromTxgId(txgId));
        if (CollectionUtils.isEmpty(resList)) {
            return txgStats;
        }

        for (BidTxgStatistics txgStat : txgStats) {
            txgStat.setList(resList.stream()
                                   .map(res -> getResStatistics(res.getResId(), txgStat.getTimestamp()))
                                   .filter(Optional::isPresent)
                                   .map(Optional::get)
                                   .collect(Collectors.toList()));
        }

        return txgStats;
    }

    private List<BidTxgStatistics> getTxgStatistics(String txgId, Date start, Date end) {
        Criteria txgCrit = Criteria.where("txgId").is(txgId)
                                   .and("statisticsType").is(StatisticsType.hour.name())
                                   .and("timestamp").gte(start).lt(end);
        Query txgQry = new Query(txgCrit).with(Sort.by(Sort.Direction.ASC, "timestamp"));

        return mongoOperations.find(txgQry, BidTxgStatistics.class);
    }

    private Optional<BidResStatistics> getResStatistics(String resId, Date timestamp) {
        Criteria resCrit = Criteria.where("resId").is(resId)
                                   .and("statisticsType").is(StatisticsType.hour.name())
                                   .and("timestamp").is(timestamp);
        Query resQry = new Query(resCrit).with(Sort.by(Sort.Direction.ASC, "timestamp"));

        return Optional.ofNullable(mongoOperations.findOne(resQry, BidResStatistics.class));
    }
}
