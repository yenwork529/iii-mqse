package org.iii.esd.mongo.repository.integrate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.*;

import org.iii.esd.enums.StatisticsType;
import org.iii.esd.mongo.document.integrate.BidTxgStatistics;

public interface BidTxgStatisticsRepository extends MongoRepository<BidTxgStatistics, Long> {

    @Query(value = "{txgId:?0, statisticsType:?1, timestamp:{$gte:?2, $lte:?3} }",
            sort = "{timestamp:1}")
    List<BidTxgStatistics> findByTxgIdAndStatisticsTypeAndTime(String txgId, StatisticsType statisticsType, Date startTime, Date endTime);
}
