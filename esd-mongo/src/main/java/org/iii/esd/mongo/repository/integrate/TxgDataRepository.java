package org.iii.esd.mongo.repository.integrate;

import java.util.*;

import org.iii.esd.mongo.document.integrate.TxgData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface TxgDataRepository extends MongoRepository<TxgData, Long> {

    @Query(value = "{ txgId:{$eq:?0}, $and:[ {startTime:{$gte:?1}}, {endTime:{$lt:?2}} ] }")
    List<TxgData> seekFromTxgId(String txgId, Date from, Date to);

}
