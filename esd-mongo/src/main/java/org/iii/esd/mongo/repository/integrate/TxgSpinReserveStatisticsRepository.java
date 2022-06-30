package org.iii.esd.mongo.repository.integrate;

import org.iii.esd.enums.StatisticsType;
import org.iii.esd.mongo.document.integrate.TxgStatistics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface TxgSpinReserveStatisticsRepository extends MongoRepository<TxgStatistics,Long> {
    @Query(value = "{ srId:{$ref:'SpinReserveProfile', $id:?0}, time:{$gte:?1, $lte:?2} }",
            sort = "{time:1}")
    List<TxgStatistics> findBySrIdAndTime(Long srId, Date start, Date end);

    @Query(value = "{ srId:{$ref:'SpinReserveProfile', $id:?0}, statisticsType:?1, time:{$gte:?2, $lte:?3} }",
            sort = "{time:1}")
    List<TxgStatistics> findBySrIdAndStatisticsTypeAndTime(Long srId, StatisticsType statisticsType, Date start, Date end);
}
