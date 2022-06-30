package org.iii.esd.mongo.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.DemandResponseProfile;

public interface DemandResponseProfileRepository extends MongoRepository<DemandResponseProfile, Long> {

    @Query(fields = "{'companyId' : 0}",
            sort = "{id:1}")
    List<DemandResponseProfile> findByIdIn(Set<Long> ids);

    List<DemandResponseProfile> findByEnableStatusNotOrderById(EnableStatus enableStatus);

}