package org.iii.esd.mongo.repository.integrate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.*;

import org.iii.esd.mongo.document.DispatchEvent;
import org.iii.esd.mongo.document.integrate.TxgDispatchEvent;
import org.iii.esd.mongo.enums.EventState;

public interface TxgDispatchEventRepository extends MongoRepository<TxgDispatchEvent, Long> {

    long countByTxgId(String txgId);

    // sort ascending(1) or descending(-1)
    @Query(value = "{ eventState:{$eq:'INIT'}, eventType:{$eq:'TYPE_A'}, actionType:{$eq:'BEGIN'} }",
            sort = "{createTime:1}")
    List<TxgDispatchEvent> seekTypeABegin();

    @Query(value = "{ eventState:{$ne:'CLOSED'}, eventType:{$eq:'TYPE_A'}, actionType:{$eq:'DONE'} }",
            sort = "{createTime:-1}")
    List<TxgDispatchEvent> seekNonClosedTypeADone();

    @Query(value = "{ eventState:{$ne:'CLOSED'}, eventType:{$eq:?0} }",
            sort = "{createTime:-1}")
    List<TxgDispatchEvent> seekNonClosedType(String typ);

    @Query(value = "{ eventState:{$ne:'CLOSED'}, eventType:{$eq:?0}, $or:[{nextPoll:{$gte:?1}},{nextPoll:{$lt:?2}}] }",
            sort = "{createTime:-1}")
    List<TxgDispatchEvent> seekNonClosedType(String typ, Date from, Date to);

    // @Query(value = "{ eventType:{$eq:'TYPE_C'}, $or:[{nextPoll:{$gte:?0}},{nextPoll:{$lt:?1}}], $or:[{serviceState:{$eq:'START'}},{serviceState:{$eq:'STOP'}}]}", sort = "{createTime:-1}")
    // @Query(value = "{ eventType:{$eq:'TYPE_C'}, $or:[serviceState:{$eq:'START'},serviceState:{$eq:'STOP'}], $or:[{nextPoll:{$gte:?0}},{nextPoll:{$lt:?1}}]}", sort = "{createTime:-1}")
    @Query(value = "{ eventType:{$eq:'TYPE_C'}, serviceState:{$ne:'ABANDON'}, $or:[{nextPoll:{$gte:?0}},{nextPoll:{$lt:?1}}]}",
            sort = "{createTime:1}")
    List<TxgDispatchEvent> seekTypeCStandBy(Date from, Date to);

    // @Query(value = "{ eventState:{$ne:'CLOSED'}, eventType:{$eq:'TYPE_C'}, $or:[serviceState:{$eq:'START'},serviceState:{$eq:'STOP'}] }", sort = "{createTime:-1}")
    // List<TxgDispatchEvent> seekNonClosedTypeCStartStop();

    // @Query(value = "{ eventState:{$eq:'INIT'} }", sort = "{nextTime:-1}")
    @Query(value = "{ eventState:{$eq:'INIT'} }",
            sort = "{createTime:-1}")
    List<TxgDispatchEvent> seekAll();

    @Query(value = "{ eventState:{$ne:'CLOSED'}, txgId:{$eq:?0}, nextPoll:{$gte:?1, $lt:?2} }",
            sort = "{createTime:-1}")
    List<TxgDispatchEvent> seekNoneClosedBetween(String txgId, Date from, Date to);

    @Query(value = "{ eventState:{$eq:'INIT'}, nextPoll:{$lte:?0} }",
            sort = "{createTime:1}")
    List<TxgDispatchEvent> seekPolls(Date dt);

    // @Query(value="{ resId: { $eq: ?0 } }", sort ="{timestamp:0}")
    // List<DrResData> findRecent(String resId, Pageable page);

    @Query(value = "{txgId:?0, updateTime:{$gte:?1, $lte: ?2}}",
            sort = "{createTime: 1}")
    List<TxgDispatchEvent> findDispatchEventByTxgIdAndDate(String txgId, Date start, Date end);

    @Query(value = "{txgId:?0, eventType:?1, eventState:?2}")
    List<TxgDispatchEvent> findByTxgIdAndEventTypeAndNotEventState(String txgId, String eventType, EventState eventState);

    @Query(value = "{" +
            "txgId: ?0," +
            "updateTime: {$gte: ?1, $lte: ?2}," +
            "eventState: {$ne: 'CLOSED'}," +
            "eventType: 'TYPE_C'" +
            "}",
            sort = "{createTime: 1}")
    List<TxgDispatchEvent> findNotClosedTypeCByTxgIdInDateTime(String txgId, Date start, Date end);

    @Query(value = "{" +
            "txgId: { $eq: ?0 }," +
            "eventType: 'TYPE_C'," +
            "$or: [" +
            "{'eventParams.abandonFromTime': { $exists: true, $gte: ?1, $lte: ?2 }}," +
            "{'eventParams.abandonToTime': { $exists: true, $gte: ?1, $lte: ?2 }}" +
            "]}",
            sort = "{createTime: 1}")
    List<TxgDispatchEvent> findAbandonByTxgIdAndDay(String txgId, Date start, Date end);

    @Query(value = "{" +
            "txgId: { $eq: ?0 }," +
            "eventType: 'TYPE_A'," +
            "actionType: 'BEGIN'," +
            "'eventParams.beginTime': { $exists: true, $gte: ?1, $lte: ?2 }" +
            "}",
            sort = "{createTime: 1}")
    List<TxgDispatchEvent> findTypeABeginByTxgIdAndDateRange(String txgId, Date start, Date end);

    @Query(value = "{" +
            "txgId: { $eq: ?0 }," +
            "eventType: 'TYPE_A'," +
            "actionType: 'RUNNING'," +
            "'eventParams.startTime': { $exists: true, $gte: ?1, $lte: ?2 }" +
            "}",
            sort = "{createTime: 1}")
    List<TxgDispatchEvent> findTypeARunningByTxgIdAndDateRange(String txgId, Date start, Date end);

    @Query(value = "{" +
            "txgId: { $eq: ?0 }," +
            "eventType: 'TYPE_A'," +
            "actionType: 'END'," +
            "'eventParams.endTime': { $exists: true, $gte: ?1, $lte: ?2 }" +
            "}",
            sort = "{createTime: 1}")
    List<TxgDispatchEvent> findTypeAEndByTxgIdAndDateRange(String txgId, Date start, Date end);

    @Query(value = "{" +
            "txgId: { $eq: ?0 }," +
            "eventType: 'TYPE_A'," +
            "actionType: 'DONE'," +
            "'eventParams.stopTime': { $exists: true, $gte: ?1, $lte: ?2 }" +
            "}",
            sort = "{createTime: 1}")
    List<TxgDispatchEvent> findTypeADoneByTxgIdAndDateRange(String txgId, Date start, Date end);

    @Query(value = "{" +
            "txgId: { $eq: ?0 }," +
            "eventType: 'TYPE_B'," +
            "alertType: {$exists: true, $eq: 'CONSUME_NOT_ENOUGH'}," +
            "createTime: { $gte: ?1, $lte: ?2 }" +
            "}",
            sort = "{createTime: 1}")
    List<TxgDispatchEvent> findTypeBByTxgIdAndDateRange(String txgId, Date start, Date end);

    @Query(value = "{" +
            "txgId: { $eq: ?0 }," +
            "eventType: 'TYPE_C'," +
            "$or: [" +
            "{'eventParams.startStandByTime': { $exists: true, $gte: ?1, $lte: ?2 }}," +
            "{'eventParams.stopStandByTime': { $exists: true, $gte: ?1, $lte: ?2 }}" +
            "]}",
            sort = "{createTime: 1}")
    List<TxgDispatchEvent> findTypeCStandByByTxgIdAndDateRange(String txgId, Date start, Date end);

    @Query(value = "{" +
            "txgId: { $eq: ?0 }," +
            "eventType: 'TYPE_C'," +
            "$or: [" +
            "{'eventParams.startServiceTime': { $exists: true, $gte: ?1, $lte: ?2 }}," +
            "{'eventParams.stopServiceTime': { $exists: true, $gte: ?1, $lte: ?2 }}" +
            "]}",
            sort = "{createTime: 1}")
    List<TxgDispatchEvent> findTypeCServiceByTxgIdAndDateRange(String txgId, Date start, Date end);

    @Query(value = "{" +
            "txgId: { $eq: ?0 }," +
            "eventType: 'TYPE_A'," +
            "actionType: 'BEGIN'," +
            "$and: [" +
            "{'eventParams.startTime': { $exists: true, $lte: ?1 }}," +
            "{'eventParams.stopTime': { $exists: true, $gte: ?1 }}" +
            "]}")
    Optional<TxgDispatchEvent> findCurrentTypeA(String txgId, Date now);
}
