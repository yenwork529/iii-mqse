package org.iii.esd.mongo.repository.integrate;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.mongo.document.integrate.RegulationHistory;

public interface RegulationHistoryRepository extends MongoRepository<RegulationHistory, String> {

    @Query(value = "{txgId: ?0, timestamp: { $gte: ?1, $lte: ?2}}")
    List<RegulationHistory> findByTxgIdAndTimestampBetween(String txgId, Date start, Date end);
}
