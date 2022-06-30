package org.iii.esd.mongo.repository.integrate;

import java.util.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.iii.esd.mongo.document.integrate.RelationshipProfile;

public interface RelationshipProfileRepository extends MongoRepository<RelationshipProfile, Long> {
    
    List<RelationshipProfile> findByTxgIdAndEndTimeIsNull(String txgId);
    List<RelationshipProfile> findByTxgIdAndEndTimeIsNotNull(String txgId);
    List<RelationshipProfile> findByResIdAndEndTimeIsNull(String ResId);
}
