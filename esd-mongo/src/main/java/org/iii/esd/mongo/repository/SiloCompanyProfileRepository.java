package org.iii.esd.mongo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import org.iii.esd.mongo.document.SiloCompanyProfile;

public interface SiloCompanyProfileRepository extends MongoRepository<SiloCompanyProfile, Long> {

    Optional<SiloCompanyProfile> findByTgCode(Integer tgCode);
}
