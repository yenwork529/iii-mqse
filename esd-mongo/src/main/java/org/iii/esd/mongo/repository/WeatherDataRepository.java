package org.iii.esd.mongo.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.enums.WeatherType;
import org.iii.esd.mongo.document.WeatherData;

public interface WeatherDataRepository extends MongoRepository<WeatherData, String> {

    @Query(value = "{ stationId:?0, time:{$gte:?1, $lte:?2} }",
            sort = "{time:1}")
    List<WeatherData> findByStationIdAndTime(String stationId, Date start, Date end);

    @Query(value = "{ stationId:?0, time:{$gte:?1, $lte:?2} }",
            delete = true)
    public void delete(String stationId, Date start, Date end);

    @Query(value = "{ stationId:?0, time:{$gte:?1, $lte:?2}, type:?3 }",
            sort = "{time:1}")
    List<WeatherData> findByStationIdAndTime(String stationId, Date start, Date end, WeatherType type);

}