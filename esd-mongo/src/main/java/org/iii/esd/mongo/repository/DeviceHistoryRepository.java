package org.iii.esd.mongo.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.mongo.document.DeviceHistory;

public interface DeviceHistoryRepository extends MongoRepository<DeviceHistory, String> {

    @Query(value = "{ deviceId:{$ref:'DeviceProfile', $id:?0}, reportTime:{$gte:?1, $lte:?2} }",
            fields = "{'deviceId': 0}",
            sort = "{reportTime:1}")
    List<DeviceHistory> findByDeviceIdAndTime(String deviceId, Date start, Date end);

    @Query(value = "{ deviceId:{$ref:'DeviceProfile', $id:?0} }",
            delete = true)
    public void delete(String deviceId);

}