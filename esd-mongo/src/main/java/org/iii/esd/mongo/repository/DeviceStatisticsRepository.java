package org.iii.esd.mongo.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.enums.StatisticsType;
import org.iii.esd.mongo.document.DeviceStatistics;

public interface DeviceStatisticsRepository extends MongoRepository<DeviceStatistics, String> {

    @Query(value = "{ deviceId:{$ref:'DeviceProfile', $id:?0}, time:{$gte:?1, $lte:?2} }",
            sort = "{time:1}")
    List<DeviceStatistics> findByDeviceIdAndTime(String deviceId, Date start, Date end);

    @Query(value = "{ deviceId:{$ref:'DeviceProfile', $id:?0}, statisticsType:?1, time:{$gte:?2, $lte:?3} }",
            sort = "{time:1}")
    List<DeviceStatistics> findByDeviceIdAndStatisticsTypeAndTime(String deviceId, StatisticsType statisticsType, Date start, Date end);

    @Query(value = "{ deviceId:{$ref:'DeviceProfile', $id:?0}, statisticsType:?1, time:{$gte:?2, $lte:?3} }",
            delete = true)
    public void delete(String deviceId, StatisticsType statisticsType, Date start, Date end);

    @Query(value = "{ deviceId:{$ref:'DeviceProfile', $id:?0} }",
            delete = true)
    public void delete(String deviceId);

}