package org.iii.esd.mongo.repository.integrate;

import java.util.*;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;

public interface TxgFieldProfileRepository extends MongoRepository<TxgFieldProfile, Long> {
    List<TxgFieldProfile> findByTcEnableNotOrderById(EnableStatus tcEnable);

    List<TxgFieldProfile> findByIdIn(Set<Long> ids);

    Optional<TxgFieldProfile> findByResId(String resId);

    List<TxgFieldProfile> findByTxgId(String txgId);

    List<TxgFieldProfile> findByTxgIdIn(Set<String> txgIds);

    @Query(value = "{ endTime:{$exists:false} }")
    List<TxgFieldProfile> seekAll();

    @Query(value = "{ startTime:{$lte:?0}, $or: [ {endTime:{$exists:false}}, {endTime:{$gt:?1}} ] }")
    List<TxgFieldProfile> seekAll(Date from, Date to);

    @Query(value = "{ resId:{$eq:?0}, endTime:{$exists:false} }")
    List<TxgFieldProfile> seekFromResId(String resId);

    @Query(value = "{ resId:{$eq:?0}, startTime:{$lte:?1}, $or: [ {endTime:{$exists:false}}, {endTime:{$gt:?2}} ] }")
    List<TxgFieldProfile> seekFromResId(String resId, Date from, Date to);

    @Query(value = "{ txgId:{$eq:?0}, endTime:{$exists:false} }")
    List<TxgFieldProfile> seekFromTxgId(String txgId);

    @Query(value = "{ txgId:{$eq:?0}, startTime:{$lte:?1}, $or: [ {endTime:{$exists:false}}, {endTime:{$gt:?2}} ] }")
    List<TxgFieldProfile> seekFromTxgId(String txgId, Date from, Date to);
}
