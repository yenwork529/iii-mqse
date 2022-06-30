package org.iii.esd.mongo.repository.integrate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.*;

import org.iii.esd.mongo.document.integrate.BidTxgBid;
import org.iii.esd.mongo.document.integrate.BidTxgInfo;

public interface BidTxgBidRepository extends MongoRepository<BidTxgBid, Long> {

    @Query(value = "{ txgId:{$eq:?0}, timestamp:{$gte:?1, $lt:?2} }", sort = "{timestamp:1}")
    List<BidTxgBid> findByTxgIdAndTime(String txgId, Date start, Date end);

    @Query(value = "{ txgId:{$eq:?0}, timestamp:{$eq:?1} }")
    BidTxgBid findByTxgIdAndTime(String txgId, Date at);
}
