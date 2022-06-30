package org.iii.esd.mongo.repository.integrate;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.mongo.document.integrate.DrResData;

public interface DrResDataRepository extends MongoRepository<DrResData, String> {

    @Query(value = "{ resId: { $eq: ?0 } }",
            sort = "{timestamp:-1}")
    List<DrResData> findRecent(String resId, Pageable page);

    // @Query(value = "{ resId:{$eq:?0}, $and:[ {timestamp:{$gte:?1}}, {timestamp:{$lt:?2}} ] }")
    // List<DrResData> seekFromResId(String resId, Date from, Date to);

    // @Query(value = "{ timeticks:{$gte:?0, $lt:?1} }", sort ="{timestamp:-1}")
    // List<DrResData> seekBetween(Long from, Long to);

    @Query(value = "{resId: ?0, timestamp: { $gte: ?1, $lte: ?2}}",
            sort = "{timestamp:1}")
    List<DrResData> findByResIdAndTime(String resId, Date from, Date to);

    @Query(sort = "{timeticks:1, resId:1}")
    List<DrResData> findByResIdInAndTimestampGreaterThanEqual(Set<String> resId, Date time);

    Optional<DrResData> findTopByResIdOrderByTimestampDesc(String resId);

    @Query(value = "{resId:?0,timestamp:{$gte:?1,$lt:?2}}",
            sort = "{timestamp:1}")
    List<DrResData> findByResIdAndTimestampBetweenLt(String resId, Date start, Date end);
}
