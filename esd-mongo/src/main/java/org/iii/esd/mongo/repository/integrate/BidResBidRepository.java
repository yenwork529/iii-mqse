package org.iii.esd.mongo.repository.integrate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.*;

import org.iii.esd.mongo.document.integrate.BidResBid;
import org.iii.esd.mongo.document.integrate.BidResInfo;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;

public interface BidResBidRepository extends MongoRepository<BidResBid, Long> {
    
    @Query(value = "{ resId:{$eq:?0}, timestamp:{$eq:?1} }")
    BidResBid findByResIdAndTime(String resId, Date at);

    @Query(value = "{ resId:{$in:?0}, timestamp:{$eq:?1} }")
    List<BidResBid> findByResIdInAndTime(Set<String> resIds, Date timestamp);
}
