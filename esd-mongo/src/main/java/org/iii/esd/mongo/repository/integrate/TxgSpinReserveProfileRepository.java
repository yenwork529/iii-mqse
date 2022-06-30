package org.iii.esd.mongo.repository.integrate;

import org.iii.esd.enums.EnableStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.iii.esd.mongo.document.integrate.TxgProfile;

import java.util.List;
import java.util.Set;

public interface TxgSpinReserveProfileRepository extends MongoRepository<TxgProfile,Long> {
    @Query(fields = "{'companyId' : 0}",
            sort = "{id:1}")
    List<TxgProfile> findByIdIn(Set<Long> ids);

    List<TxgProfile> findByEnableStatusNotOrderById(EnableStatus enableStatus);
}
