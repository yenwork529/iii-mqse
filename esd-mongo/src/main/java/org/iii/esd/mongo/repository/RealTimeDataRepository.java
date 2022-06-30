package org.iii.esd.mongo.repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.mongo.document.RealTimeData;

public interface RealTimeDataRepository extends MongoRepository<RealTimeData, String> {

    @Query(fields = "{'deviceId': 0}",
            sort = "{id:1}")
    List<RealTimeData> findByIdIn(Set<String> ids);

    @Query(value = "{ reportTime:{$lte:?0} }",
            sort = "{reportTime:1}")
    List<RealTimeData> findBeforeReportTime(Date reportTime);

}