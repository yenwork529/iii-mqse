package org.iii.esd.mongo.repository.integrate;

import java.util.*;

import org.iii.esd.mongo.document.integrate.TxgBid;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface TxgBidRepository extends MongoRepository<TxgBid, Long> {

    @Query(value = "{ txgId:{$eq:?0}, $and:[ {timestamp:{$gte:?1}}, {timestamp:{$lt:?2}} ] }")
    List<TxgBid> seekFromTxgId(String txgId, Date from, Date to);

}
