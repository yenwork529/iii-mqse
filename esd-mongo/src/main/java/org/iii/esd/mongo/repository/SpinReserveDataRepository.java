package org.iii.esd.mongo.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.enums.NoticeType;
import org.iii.esd.mongo.document.SpinReserveData;
import org.iii.esd.mongo.document.SpinReserveProfile;

public interface SpinReserveDataRepository extends MongoRepository<SpinReserveData, String> {

    @Query(value = "{ srId:{$ref:'SpinReserveProfile', $id:?0}, noticeTime:{$gte:?1, $lte:?2} }",
            fields = "{'srId' : 0}",
            sort = "{time:1}")
    List<SpinReserveData> findBySrIdAndNoticeTime(Long srId, Date start, Date end);

    @Query(value = "{ srId:{$ref:'SpinReserveProfile', $id:?0}, noticeType:?1, noticeTime:{$gte:?2, $lte:?3} }",
            fields = "{'srId' : 0}",
            sort = "{time:1}")
    List<SpinReserveData> findBySrIdAndNoticeTypeAndNoticeTime(Long srId, NoticeType noticeType, Date start, Date end);

    @Query(value = "{ srId:{$ref:'SpinReserveProfile', $id:?0}, noticeType:?1, startTime:{$gte:?2, $lt:?3} }",
            fields = "{'srId' : 0}",
            sort = "{time:1}")
    List<SpinReserveData> findBySrIdAndNoticeTypeAndStartTime(Long srId, NoticeType noticeType, Date start, Date end);

    @Query(value = "{ srId:{$ref:'SpinReserveProfile', $id:?0}, noticeType:?1, startTime:{$gte:?2, $lt:?3} }",
            fields = "{'srId' : 0}",
            sort = "{time:1}")
    Optional<SpinReserveData> findSpinReserveDataBySrIdAndNoticeTypeInCurrentTime(long srId, NoticeType noticeType, Date currentTime);
}