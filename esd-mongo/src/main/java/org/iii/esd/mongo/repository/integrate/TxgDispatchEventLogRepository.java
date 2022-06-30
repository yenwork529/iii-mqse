package org.iii.esd.mongo.repository.integrate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

import org.iii.esd.mongo.document.integrate.TxgDispatchEventLog;

public interface TxgDispatchEventLogRepository extends MongoRepository<TxgDispatchEventLog, Long> {
 
    // @Query(value="{ resId: { $eq: ?0 } }", sort ="{timestamp:0}")
	// List<DrResData> findRecent(String resId,  Pageable page);
}
