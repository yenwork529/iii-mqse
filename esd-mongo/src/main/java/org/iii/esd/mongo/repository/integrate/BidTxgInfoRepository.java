package org.iii.esd.mongo.repository.integrate;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.mongo.document.integrate.BidTxgInfo;

public interface BidTxgInfoRepository extends MongoRepository<BidTxgInfo, Long> {
    @Query(value = "{ txgId:{$eq:?0}, timestamp:{$gte:?1, $lt:?2} }",
            sort = "{timestamp:1}")
    List<BidTxgInfo> findByTxgIdAndTime(String txgId, Date start, Date end);

    @Query(value = "{ txgId:{$eq:?0}, timestamp:{$eq:?1} }")
    BidTxgInfo findByTxgIdAndTime(String txgId, Date at);
}
