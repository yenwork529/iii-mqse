package org.iii.esd.mongo.repository.integrate;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.mongo.document.integrate.SettlementPrice;

public interface SettlementPriceRepository extends MongoRepository<SettlementPrice, Long> {

    @Query(value = "{ $and:[ {timestamp: {$gte:?0}}, {timestamp: {$lt:?1}} ] }")
    List<SettlementPrice> seekBetween(Long from, Long to);

    @Query(value = "{ $and:[ {timestamp: {$gte:?0}}, {timestamp: {$lte:?1}} ] }")
    List<SettlementPrice> findByTimestamp(Date start, Date end);

    @Query(value = "{ $and:[ {timestamp: {$gte:?0}}, {timestamp: {$lt:?1}} ] }")
    List<SettlementPrice> findByTimestampBetween(Date start, Date end);
}
