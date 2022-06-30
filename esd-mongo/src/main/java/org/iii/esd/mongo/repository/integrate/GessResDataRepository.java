package org.iii.esd.mongo.repository.integrate;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.mongo.document.integrate.DrResData;
import org.iii.esd.mongo.document.integrate.GessResData;

public interface GessResDataRepository extends MongoRepository<GessResData, String> {

    @Query(value = "{resId:?0,timestamp:{$gte:?1,$lte:?2}}",
            sort = "{timestamp:1}")
    List<GessResData> findByResIdAndTimestampBetween(String resId, Date start, Date end);

    @Query(value = "{resId:?0,timestamp:{$gte:?1,$lt:?2}}",
            sort = "{timestamp:1}")
    List<GessResData> findByResIdAndTimestampBetweenLt(String resId, Date start, Date end);

    Optional<GessResData> findTopByResIdOrderByTimestampDesc(String resId);

    @Query(sort = "{timeticks:1, resId:1}")
    List<GessResData> findByResIdInAndTimestampGreaterThanEqual(Set<String> resId, Date time);

}
