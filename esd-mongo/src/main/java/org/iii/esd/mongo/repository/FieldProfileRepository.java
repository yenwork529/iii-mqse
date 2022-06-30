package org.iii.esd.mongo.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.SpinReserveProfile;

public interface FieldProfileRepository extends MongoRepository<FieldProfile, Long> {

    List<FieldProfile> findByTcEnableNotOrderById(EnableStatus tcEnable);

    List<FieldProfile> findBySpinReserveProfileAndSrIndexNotNullOrderBySrIndex(SpinReserveProfile spinReserveProfile);

    List<FieldProfile> findBySpinReserveProfileAndSrIndexNotNullOrderByResCode(SpinReserveProfile spinReserveProfile);

    List<FieldProfile> findByIdIn(Set<Long> ids);

    //@Query(value="{ srId:{$ref:'SpinReserveProfile', $id:?0}}")
    int countBySpinReserveProfile(SpinReserveProfile spinReserveProfile);

}