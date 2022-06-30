package org.iii.esd.server.services;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.iii.esd.enums.StatisticsType;
import org.iii.esd.mongo.document.integrate.BidResStatistics;
import org.iii.esd.mongo.document.integrate.BidTxgBid;
import org.iii.esd.mongo.document.integrate.BidTxgStatistics;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.repository.integrate.BidResStatisticsRepository;
import org.iii.esd.mongo.repository.integrate.BidTxgStatisticsRepository;
import org.iii.esd.mongo.service.integrate.IntegrateDataService;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.utils.PredicateUtils;

@Service
public class IntegrateStatService {

    @Autowired
    private BidTxgStatisticsRepository txgStatRepository;
    @Autowired
    private BidResStatisticsRepository resStatRepository;
    @Autowired
    private IntegrateRelationService relationService;
    @Autowired
    private TxgFieldService resService;
    @Autowired
    private IntegrateBidService bidService;
    @Autowired
    private IntegrateDataService dataService;

    public List<BidTxgStatistics> findStatByTxgIdAndStatisticsTypeAndTime(
            String txgId, StatisticsType statisticsType, Date startTime, Date endTime) {
        List<BidTxgStatistics> txgStats = txgStatRepository.findByTxgIdAndStatisticsTypeAndTime(txgId, statisticsType, startTime, endTime);
        List<TxgFieldProfile> resList = resService.findByTxgId(txgId);
        Set<String> resIds = resList.stream()
                                    .map(TxgFieldProfile::getResId)
                                    .collect(Collectors.toSet());
        List<BidResStatistics> resStats = resStatRepository.findByResIdInAndTimestampBetween(resIds,statisticsType, startTime, endTime);

        for (BidTxgStatistics txgStat : txgStats) {
            txgStat.setList(resStats.stream()
                                    .filter(PredicateUtils.isEqualsTo(txgStat.getTimestamp(), BidResStatistics::getTimestamp))
                                    .sorted(Comparator.comparing(BidResStatistics::getResId))
                                    .collect(Collectors.toList()));
        }

        return txgStats;

        /*
        List<BidTxgBid> txgBid = bidService.findBidByTxgIdAndTime(txgId, startTime, endTime);
        return txgBid.stream()
                .map(BidTxgStatistics::fromBid)
                .collect(Collectors.toList());
        */
    }
}
