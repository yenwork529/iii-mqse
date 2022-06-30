package org.iii.esd.mongo.repository.integrate;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import org.iii.esd.mongo.document.integrate.TxgCompanyProfile;

public interface TxgCompanyProfileRepository extends MongoRepository<TxgCompanyProfile, Long> {
    Optional<TxgCompanyProfile> findByCompanyId(String companyId);
}
