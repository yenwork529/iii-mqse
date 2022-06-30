package org.iii.esd.mongo.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.enums.StatisticsType;
import org.iii.esd.mongo.document.SpinReserveStatistics;

public interface SpinReserveStatisticsRepository extends MongoRepository<SpinReserveStatistics, String> {

    @Query(value = "{ srId:{$ref:'SpinReserveProfile', $id:?0}, time:{$gte:?1, $lte:?2} }",
            sort = "{time:1}")
    List<SpinReserveStatistics> findBySrIdAndTime(Long srId, Date start, Date end);

    @Query(value = "{ srId:{$ref:'SpinReserveProfile', $id:?0}, statisticsType:?1, time:{$gte:?2, $lte:?3} }",
            sort = "{time:1}")
    List<SpinReserveStatistics> findBySrIdAndStatisticsTypeAndTime(Long srId, StatisticsType statisticsType, Date start, Date end);

}