package org.iii.esd.mongo.repository.integrate;

import org.iii.esd.enums.StatisticsType;
import org.iii.esd.mongo.document.integrate.BidTxgStatistics;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.*;

import org.iii.esd.mongo.document.integrate.BidResStatistics;

public interface BidResStatisticsRepository extends MongoRepository<BidResStatistics, Long> {

//    @Query(value = "{ resId: {$in:?0}, timestamp: {$gte:?1, $lte:?2}}")
//    List<BidResStatistics> findByResIdInAndTimestampBetween(Set<String> resIds, Date startTime, Date endTime);

    @Query(value = "{ resId: {$in:?0},statisticsType:?1, timestamp: {$gte:?2, $lte:?3}}")
    List<BidResStatistics> findByResIdInAndTimestampBetween(Set<String> resIds, StatisticsType statisticsType, Date startTime, Date endTime);

//    @Query(value = "{resId:?0, statisticsType:?1, timestamp: {$gte:?1, $lte:?2} }",
//            sort = "{time:1}")
//    List<BidResStatistics> findByResIdAndStatisticsTypeAndTime(String resId, StatisticsType statisticsType, Date startTime, Date endTime);
}
