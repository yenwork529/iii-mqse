package org.iii.esd.mongo.repository.integrate;

import java.util.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.iii.esd.mongo.document.integrate.DrTxgData;

public interface DrTxgDataRepository extends MongoRepository<DrTxgData, String> {
        
    // @Query(value = "{ txgId:{$eq:?0}, endTime:{$exists:false} }")
    // List<DrTxgData> seekFromTxgId(String txgId);

    @Query(value = "{ txgId:{$eq:?0}, $and:[ {timestamp:{$gte:?1}}, {timestamp:{$lt:?2}} ] }")
    List<DrTxgData> seekFromTxgId(String txgId, Date from, Date to);

    @Query(value = "{ txgId:{$eq:?0}, $and:[ {timestamp:{$gte:?1}}, {timestamp:{$lte:?2}} ] }")
    List<DrTxgData> findFromTxgIdAndTimestamp(String txgId, Date from, Date to);
}
