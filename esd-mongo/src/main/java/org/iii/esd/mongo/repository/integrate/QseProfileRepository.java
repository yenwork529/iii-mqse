package org.iii.esd.mongo.repository.integrate;

import java.util.Optional;
import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.mongo.document.integrate.QseProfile;

public interface QseProfileRepository extends MongoRepository<QseProfile, Long> {
    Optional<QseProfile> findByQseId(String qseId);

    @Query(value = "{ endTime:{$exists:false} }")
    List<QseProfile> seekAll();

    @Query(value = "{ startTime:{$lte:?0}, $or: [ {endTime:{$exists:false}}, {endTime:{$gt:?1}} ] }")
    List<QseProfile> seekAll(Date from, Date to);
}
