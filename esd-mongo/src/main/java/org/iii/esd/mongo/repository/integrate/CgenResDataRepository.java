package org.iii.esd.mongo.repository.integrate;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.iii.esd.mongo.document.integrate.CgenResData;

public interface CgenResDataRepository extends MongoRepository<CgenResData, Long> {
    
}
