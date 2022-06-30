package org.iii.esd.mongo.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.mongo.document.SpinReserveBid;

public interface SpinReserveBidRepository extends MongoRepository<SpinReserveBid, Long> {

    @Query(value = "{ srId:{$ref:'SpinReserveProfile', $id:?0}, timestamp:{$gte:?1, $lt:?2} }",
            sort = "{timestamp:1}")
    List<SpinReserveBid> findBySrIdAndTime(Long srId, Date start, Date end);

    @Query(value = "{ srId:{$ref:'SpinReserveProfile', $id:?0} }",
            sort = "{timestamp:1}")
    List<SpinReserveBid> findBySrId(Long srId);
}