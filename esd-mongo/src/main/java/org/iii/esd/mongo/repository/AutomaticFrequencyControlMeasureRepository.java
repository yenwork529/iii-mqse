package org.iii.esd.mongo.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.mongo.document.AutomaticFrequencyControlMeasure;

public interface AutomaticFrequencyControlMeasureRepository extends MongoRepository<AutomaticFrequencyControlMeasure, Long> {

    @Query(value = "{ afcId:{$ref:'AutomaticFrequencyControlProfile', $id:?0}, timestamp:?1, type:?2 }",
            sort = "{timestamp:1}")
    List<AutomaticFrequencyControlMeasure> findByAfcIdAndTimeAndType(Long afcId, Date timestamp, String type);

    @Query(value = "{ afcId:{$ref:'AutomaticFrequencyControlProfile', $id:?0}, timestamp:{$gte:?1, $lt:?2}, type:?3 }",
            sort = "{timestamp:1}")
    List<AutomaticFrequencyControlMeasure> findByAfcIdAndTimeAndType(Long srId, Date start, Date end, String type);
}
