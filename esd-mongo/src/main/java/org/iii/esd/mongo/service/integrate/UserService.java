package org.iii.esd.mongo.service.integrate;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import org.iii.esd.exception.ApplicationException;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.QseProfile;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.document.integrate.UserProfile;
import org.iii.esd.mongo.repository.integrate.UserProfileRepository;
import org.iii.esd.mongo.util.ModelHelper;
import org.iii.esd.utils.DatetimeUtils;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@Log4j2
public class UserService {

    @Autowired
    private UserProfileRepository userRepository;
    @Autowired
    private IntegrateRelationService relationService;
    @Autowired
    private QseService qseService;
    @Autowired
    private TxgService txgService;
    @Autowired
    private TxgFieldService resService;
    @Autowired
    private MongoOperations mongoOperations;

    public void create(UserProfile user) {
        ModelHelper.checkIdentity(user);

        userRepository.findByEmail(user.getEmail())
                      .ifPresentOrElse(ModelHelper::duplicatedIdentity,
                              () -> {
                                  user.setCompanyId(getCompanyIdByOrg(user.getOrgId()));
                                  user.initial();
                                  userRepository.save(user);
                              });
    }

    private String getCompanyIdByOrg(UserProfile.OrgId orgId) {
        switch (orgId.getType()) {
            case QSE:
                QseProfile qse = qseService.getQseList().get(0);
                return qse.getCompanyId();
            case TXG:
                TxgProfile txg = txgService.findByTxgId(orgId.getId());
                return txg.getCompanyId();
            case RES:
            default:
                TxgFieldProfile res = resService.findByResId(orgId.getId());
                return res.getCompanyId();
        }
    }

    public void update(UserProfile updated) {
        ModelHelper.checkIdentity(updated);

        userRepository.findByEmail(updated.getEmail())
                      .ifPresent(curr -> {
                          ModelHelper.checkPassword(updated, curr);

                          updated.setCompanyId(getCompanyIdByOrg(updated.getOrgId()));

                          ModelHelper.copyProperties(updated, curr);

                          userRepository.save(curr);
                      });
    }

    public UserProfile getByEmail(String email) throws WebException {
        return userRepository.findByEmail(email)
                             .orElseThrow(WebException.of(Error.noData, email));
    }

    public UserProfile findByEmail(String email) {
        return userRepository.findByEmail(email)
                             .orElseThrow(ApplicationException.of(Error.noData, email));
    }

    public UserProfile getById(long id) throws WebException {
        return userRepository.findById(id)
                             .orElseThrow(WebException.of(Error.noData, id));
    }

    public Optional<UserProfile> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<UserProfile> getAll() {
        return userRepository.findAll();
    }


    public Boolean exists(String email) {
        return userRepository.existsByEmail(email);
    }

    public Boolean checkPasswordRetryAttempts(String email, int retry, int banMins) {
        return userRepository.existsByEmailAndRetryGreaterThenAndLastLoginTimeGreaterThen(
                email, retry, DatetimeUtils.add(new Date(), Calendar.MINUTE, -banMins));
    }

    public void resetPasswordRetry(String email) {
        mongoOperations.updateFirst(
                               query(where("email").is(email)),
                               new Update().set("retry", 0)
                                           .set("lastLoginTime", new Date()),
                               UserProfile.class)
                       .isModifiedCountAvailable();
    }

    public void updatePasswordRetryAttempts(String email) {
        mongoOperations.updateFirst(
                               query(where("email").is(email)),
                               new Update()
                                       .inc("retry", 1)
                                       .set("lastLoginTime", new Date()),
                               UserProfile.class)
                       .isModifiedCountAvailable();
    }

    public Boolean updateToken(String email, String token) {
        if (userRepository.existsByEmail(email)) {
            UserProfile userProfile = findByEmail(email);
            userProfile.setLineToken(token);
            update(userProfile);
            return true;
        }

        return false;
    }

}
