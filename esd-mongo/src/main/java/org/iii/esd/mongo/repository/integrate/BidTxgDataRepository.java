package org.iii.esd.mongo.repository.integrate;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.enums.NoticeType;
import org.iii.esd.mongo.document.integrate.BidTxgData;

public interface BidTxgDataRepository extends MongoRepository<BidTxgData, Long> {

    @Query(value = "{ txgId:?0, noticeTime:{$gte:?1, $lte:?2} }",
            sort = "{time:1}")
    List<BidTxgData> findByTxgIdAndNoticeTime(String txgId, Date start, Date end);

    @Query(value = "{ txgId:?0, noticeType:?1, noticeTime:{$gte:?2, $lte:?3} }",
            sort = "{time:1}")
    List<BidTxgData> findByTxgIdAndNoticeTypeAndNoticeTime(String txgId, NoticeType noticeType, Date start, Date end);

    @Query(value = "{ txgId:?0, noticeType:?1, noticeTime:?2 }")
    Optional<BidTxgData> findByTxgIdAndNoticeTypeAndNoticeTimeAt(String txgId, NoticeType noticeType, Date noticeTime);

    @Query(value = "{ txgId:?0, noticeType:?1, startTime:{$gte:?2, $lt:?3} }",
            sort = "{time:1}")
    List<BidTxgData> findByTxgIdAndNoticeTypeAndStartTime(String txgId, NoticeType noticeType, Date start, Date end);

    @Query(value = "{ txgId:?0, noticeType:'UNLOAD', startTime:{$lte:?1}, endTime:{$gte:?1}}")
    Optional<BidTxgData> findUnloadByTxgIdInCurrentTime(String txgId, Date currentTime);

}
