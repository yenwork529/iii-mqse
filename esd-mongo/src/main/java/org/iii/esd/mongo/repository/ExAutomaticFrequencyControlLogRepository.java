package org.iii.esd.mongo.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.mongo.document.ExAutomaticFrequencyControlLog;

public interface ExAutomaticFrequencyControlLogRepository extends MongoRepository<ExAutomaticFrequencyControlLog, Long> {
//     @Query(value = "{ afcId:{$ref:'AutomaticFrequencyControlProfile', $id:?0}, timestamp:?1 }",
//             sort = "{timestamp:1}")
//     List<AutomaticFrequencyControlLog> findByAfcIdAndTime(Long afcId, Date timestamp);

//     @Query(value = "{ afcId:{$ref:'AutomaticFrequencyControlProfile', $id:?0}, timestamp:{$gte:?1, $lt:?2} }",
//             sort = "{timestamp:1}")
//     List<AutomaticFrequencyControlLog> findByAfcIdAndTime(Long srId, Date start, Date end);
}
