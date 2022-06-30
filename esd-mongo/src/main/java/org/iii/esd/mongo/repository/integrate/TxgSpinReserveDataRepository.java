package org.iii.esd.mongo.repository.integrate;

import org.iii.esd.enums.NoticeType;
import org.iii.esd.mongo.document.integrate.TxgData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TxgSpinReserveDataRepository extends MongoRepository<TxgData,Long> {
    @Query(value = "{ srId:{$ref:'SpinReserveProfile', $id:?0}, noticeTime:{$gte:?1, $lte:?2} }",
            fields = "{'srId' : 0}",
            sort = "{time:1}")
    List<TxgData> findBySrIdAndNoticeTime(Long srId, Date start, Date end);

    @Query(value = "{ srId:{$ref:'SpinReserveProfile', $id:?0}, noticeType:?1, noticeTime:{$gte:?2, $lte:?3} }",
            fields = "{'srId' : 0}",
            sort = "{time:1}")
    List<TxgData> findBySrIdAndNoticeTypeAndNoticeTime(Long srId, NoticeType noticeType, Date start, Date end);

    @Query(value = "{ srId:{$ref:'SpinReserveProfile', $id:?0}, noticeType:?1, startTime:{$gte:?2, $lt:?3} }",
            fields = "{'srId' : 0}",
            sort = "{time:1}")
    List<TxgData> findBySrIdAndNoticeTypeAndStartTime(Long srId, NoticeType noticeType, Date start, Date end);

    @Query(value = "{ srId:{$ref:'SpinReserveProfile', $id:?0}, noticeType:?1, startTime:{$gte:?2, $lt:?3} }",
            fields = "{'srId' : 0}",
            sort = "{time:1}")
    Optional<TxgData> findTxgDataBySrIdAndNoticeTypeInCurrentTime(long srId, NoticeType noticeType, Date currentTime);
}
