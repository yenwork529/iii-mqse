package org.iii.esd.mongo.repository.integrate;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.mongo.document.integrate.BidResData;
import org.iii.esd.mongo.document.integrate.BidTxgData;

public interface BidResDataRepository extends MongoRepository<BidResData, Long> {

    @Query(value = "{ resId: {$eq: ?0}, noticeTime: {$eq: ?1}}")
    BidResData findByResIdAndNoticeTime(String resId, Date noticeTime);

    @Query(value = "{ resId: {$in: ?0}, noticeTime: {$eq: ?1}}")
    List<BidResData> findByResIdInAndNoticeTime(Set<String> resIds, Date noticeTime);

    @Query(value = "{ resId:?0, startTime:{$lte:?1}, endTime:{$gte:?1}}")
    Optional<BidResData> findByResIdInCurrentTime(String resId, Date now);
}
