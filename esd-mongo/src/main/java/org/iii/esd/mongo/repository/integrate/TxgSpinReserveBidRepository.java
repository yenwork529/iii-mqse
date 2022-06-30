package org.iii.esd.mongo.repository.integrate;

import org.iii.esd.mongo.document.integrate.TxgBid;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface TxgSpinReserveBidRepository extends MongoRepository<TxgBid,Long> {
    @Query(value = "{ srId:{$ref:'SpinReserveProfile', $id:?0}, timestamp:{$gte:?1, $lt:?2} }",
            sort = "{timestamp:1}")
    List<TxgBid> findBySrIdAndTime(Long srId, Date start, Date end);

    @Query(value = "{ srId:{$ref:'SpinReserveProfile', $id:?0} }",
            sort = "{timestamp:1}")
    List<TxgBid> findBySrId(Long srId);
}