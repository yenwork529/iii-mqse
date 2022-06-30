package org.iii.esd.mongo.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import org.iii.esd.exception.Error;
import org.iii.esd.exception.IiiException;
import org.iii.esd.mongo.document.SiloUserProfile;
import org.iii.esd.mongo.repository.SiloUserProfileRepository;
import org.iii.esd.utils.DatetimeUtils;
import org.iii.esd.utils.ValidationUtils;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@Log4j2
public class SiloUserService {

    @Autowired
    private SiloUserProfileRepository userProfileRepo;

    @Autowired
    private UpdateService updateService;

    @Autowired
    private MongoOperations mongoOperations;

    public long add(SiloUserProfile siloUserProfile) {
        try {
            String email = siloUserProfile.getEmail();
            if (StringUtils.isNotBlank(email) && ValidationUtils.isEmailValid(email)) {
                siloUserProfile.setId(updateService.genSeq(SiloUserProfile.class));
                siloUserProfile.setCreateTime(new Date());
                userProfileRepo.insert(siloUserProfile);
                return siloUserProfile.getId();
            } else {
                throw new IiiException(Error.emailFormatInvalid);
            }
        } catch (DuplicateKeyException e) {
            log.error(e.getMessage());
            throw new IiiException(Error.emailAddressAlreadyExists);
        }
    }

    public SiloUserProfile update(SiloUserProfile siloUserProfile) {
        if (ValidationUtils.isEmailValid(siloUserProfile.getEmail())) {
            return userProfileRepo.save(siloUserProfile);
        } else {
            throw new IiiException(Error.emailFormatInvalid);
        }
    }

    public boolean updatePasswordRetryAttempts(String email) {
        return mongoOperations.updateFirst(
                                      query(where("email").is(email)),
                                      new Update()
                                              .inc("retry", 1)
                                              .set("lastLoginTime", new Date()),
                                      SiloUserProfile.class).
                              isModifiedCountAvailable();
    }

    public boolean resetPasswordRetry(String email) {
        return mongoOperations.updateFirst(
                                      query(where("email").is(email)),
                                      new Update().set("retry", 0)
                                                  .set("lastLoginTime", new Date()),
                                      SiloUserProfile.class).
                              isModifiedCountAvailable();
    }

    public Optional<SiloUserProfile> find(Long id) {
        return userProfileRepo.findById(id);
    }

    public SiloUserProfile findByEmail(String email) {
        return userProfileRepo.findOneByEmail(email);
    }

    public List<SiloUserProfile> findAll() {
        return userProfileRepo.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public List<SiloUserProfile> findByExample(Long companyId, Long fieldId, Long afcId, Long drId, Long srId) {
        return userProfileRepo.findAll(
                Example.of(new SiloUserProfile(companyId, fieldId, afcId, drId, srId)),
                Sort.by(Sort.Direction.ASC, "id"));
    }

    public List<SiloUserProfile> findByRoleIds(Long roleid) {
        return userProfileRepo.findByRoleIds(roleid);
    }

    public Boolean exists(String email) {
        return userProfileRepo.existsByEmail(email);
    }

    public Boolean checkPasswordRetryAttempts(String email, int retry, int banMins) {
        return userProfileRepo.existsByEmailAndRetryGreaterThenAndLastLoginTimeGreaterThen(email, retry,
                DatetimeUtils.add(new Date(), Calendar.MINUTE, -banMins));
    }

    public void delete(Long id) {
        userProfileRepo.deleteById(id);
    }

    public Boolean updateToken(String email, String token) {
        if (userProfileRepo.existsByEmail(email)) {
            SiloUserProfile siloUserProfile = findByEmail(email);
            siloUserProfile.setLineToken(token);
            update(siloUserProfile);
            return true;
        }
        return false;
    }

}