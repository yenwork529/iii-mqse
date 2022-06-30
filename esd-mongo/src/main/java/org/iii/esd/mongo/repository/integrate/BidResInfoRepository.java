package org.iii.esd.mongo.repository.integrate;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.mongo.document.integrate.BidResInfo;

public interface BidResInfoRepository extends MongoRepository<BidResInfo, Long> {
    @Query(value = "{resId:{$eq:?0}, timestamp:{$eq:?1}}")
    BidResInfo findByResIdAndTime(String resId, Date at);

    @Query(value = "{resId:{$in:?0}, timestamp:{$eq:?1}}")
    List<BidResInfo> findByResIdInAndTime(Set<String> resId, Date at);
}
