package org.iii.esd.mongo.repository.integrate;

import java.util.*;

import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface TxgProfileRepository extends MongoRepository<TxgProfile, Long> {

    @Query(value = "{ endTime:{$exists:false} }")
    List<TxgProfile> seekAll();

    @Query(value = "{ startTime:{$lte:?0}, $or: [ {endTime:{$exists:false}}, {endTime:{$gt:?1}} ] }")
    List<TxgProfile> seekAll(Date from, Date to);

    @Query(value = "{ qseId:{$eq:?0}, endTime:{$exists:false} }")
    List<TxgProfile> seekFromQseId(String qseId);

    @Query(value = "{ qseId:{$eq:?0}, startTime:{$lte:?1}, $or: [ {endTime:{$exists:false}}, {endTime:{$gt:?2}} ] }")
    List<TxgProfile> seekFromQseId(String qseId, Date from, Date to);

    @Query(value = "{ txgCode:{$eq:?0}, endTime:{$exists:false} }")
    List<TxgProfile> seekFromTxgCode(Integer txgCode);

    @Query(value = "{ txgCode:{$eq:?0}, startTime:{$lte:?1}, $or: [ {endTime:{$exists:false}}, {endTime:{$gt:?2}} ] }")
    List<TxgProfile> seekFromTxgCode(Integer txgCode, Date from, Date to);

    @Query(value = "{ txgId:{$eq:?0}, endTime:{$exists:false} }")
    List<TxgProfile> seekFromTxgId(String txgId);

    @Query(value = "{ txgId:{$eq:?0}, startTime:{$lte:?1}, $or: [ {endTime:{$exists:false}}, {endTime:{$gt:?2}} ] }")
    List<TxgProfile> seekFromTxgId(String txgId, Date from, Date to);

    List<TxgProfile> findByQseId(String qseId);

    Optional<TxgProfile> findByTxgId(String txgId);

}
