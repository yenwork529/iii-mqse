package org.iii.esd.mongo.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.mongo.document.DispatchEvent;
import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.mongo.enums.EventState;

public interface DispatchEventRepository extends MongoRepository<DispatchEvent, String> {
    long countBySpinReserveProfile(SpinReserveProfile spinReserveProfile);

    @Query(value = "{ srId:{$ref:'SpinReserveProfile', $id:?0}, eventState:{$ne:?1} }",
            sort = "{createTime: 1}")
    List<DispatchEvent> findBySpinReserveProfileAndNotEventState(long srId, String eventState);

    @Query(value = "{ srId:{$ref:'SpinReserveProfile', $id:?0}, updateTime:{$gte:?1, $lte: ?2} }",
            sort = "{createTime: 1}")
    List<DispatchEvent> findBySpinReserveProfileInDateTime(Long srId, Date start, Date end);

    @Query(value = "{" +
            "srId:{$ref:'SpinReserveProfile', $id:?0}," +
            "updateTime:{$gte:?1, $lte: ?2}," +
            "eventState:{$ne:'CLOSED'}" +
            "}",
            sort = "{createTime: 1}")
    List<DispatchEvent> findNotClosedBySpinReserveProfileInDateTime(Long srId, Date start, Date end);

    @Query(value = "{" +
            "srId: {$ref: 'SpinReserveProfile', $id: ?0}," +
            "updateTime: {$gte: ?1, $lte: ?2}," +
            "eventState: {$ne: 'CLOSED'}," +
            "eventType: 'TYPE_C'" +
            "}",
            sort = "{createTime: 1}")
    List<DispatchEvent> findNotClosedTypeCBySpinReserveProfileInDateTime(Long srId, Date start, Date end);

    @Query(value = "{" +
            "srId: {$ref: 'SpinReserveProfile', $id: ?0}" +
            "}",
            sort = "{createTime: 1}")
    List<DispatchEvent> findBySpinReserveProfile(Long srId);

    @Query(value = "{" +
            "srId:{$ref:'SpinReserveProfile', $id:?0}," +
            "eventType: ?1," +
            "eventState:{$ne:?2}" +
            "}",
            sort = "{createTime: 1}")
    List<DispatchEvent> findBySpinReserveProfileEventTypeAndNotEventState(Long srId, String eventType, EventState eventState);

    @Query(value = "{" +
            "srId: { $ref: 'SpinReserveProfile', $id: ?0 }," +
            "eventType: 'TYPE_C'," +
            "$or: [" +
            "{'eventParams.abandonFromTime': { $exists: true, $gte: ?1, $lte: ?2 }}," +
            "{'eventParams.abandonToTime': { $exists: true, $gte: ?1, $lte: ?2 }}" +
            "]}",
            sort = "{createTime: 1}")
    List<DispatchEvent> findAbandonBySpinReserveProfileAndDay(Long srId, Date start, Date end);
}
