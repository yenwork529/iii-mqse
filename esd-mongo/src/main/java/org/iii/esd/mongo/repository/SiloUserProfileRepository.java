package org.iii.esd.mongo.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.mongo.document.SiloUserProfile;

public interface SiloUserProfileRepository extends MongoRepository<SiloUserProfile, Long> {

    SiloUserProfile findOneByEmail(String email);

    Boolean existsByEmail(String email);

    @Query(value = "{ email:?0, retry:{$gt:?1}, lastLoginTime:{$gt:?2} }",
            exists = true)
    Boolean existsByEmailAndRetryGreaterThenAndLastLoginTimeGreaterThen(String email, int retry, Date time);

    List<SiloUserProfile> findByRoleIds(Long roleid);

}
