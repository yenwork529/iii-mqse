package org.iii.esd.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import org.iii.esd.mongo.document.PolicyProfile;

public interface PolicyProfileRepository extends MongoRepository<PolicyProfile, Long> {

}
