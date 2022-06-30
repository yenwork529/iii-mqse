package org.iii.esd.mongo.repository.integrate;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.mongo.document.integrate.UserProfile;

public interface UserProfileRepository extends MongoRepository<UserProfile, Long> {
    Optional<UserProfile> findByEmail(String email);

    Boolean existsByEmail(String email);

    @Query(value = "{ email:?0, retry:{$gt:?1}, lastLoginTime:{$gt:?2} }",
            exists = true)
    Boolean existsByEmailAndRetryGreaterThenAndLastLoginTimeGreaterThen(String email, int retry, Date time);

}
