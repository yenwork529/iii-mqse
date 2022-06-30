package org.iii.esd.mongo.repository.integrate;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import org.iii.esd.mongo.document.integrate.CompanyProfile;

public interface CompanyProfileRepository extends MongoRepository<CompanyProfile, Long> {
    Optional<CompanyProfile> findByCompanyId(String companyId);
}
