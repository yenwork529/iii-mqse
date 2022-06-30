package org.iii.esd.mongo.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.SpinReserveProfile;

public interface SpinReserveProfileRepository extends MongoRepository<SpinReserveProfile, Long> {

    @Query(fields = "{'companyId' : 0}",
            sort = "{id:1}")
    List<SpinReserveProfile> findByIdIn(Set<Long> ids);

    List<SpinReserveProfile> findByEnableStatusNotOrderById(EnableStatus enableStatus);

}