package org.iii.esd.server.controllers.rest.esd.aaa;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import org.iii.esd.exception.ApplicationException;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.QseProfile;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.document.integrate.UserProfile;
import org.iii.esd.mongo.service.integrate.IntegrateDataService;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.mongo.service.integrate.QseService;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.mongo.service.integrate.UserService;
import org.iii.esd.server.services.IntegrateBidService;
import org.iii.esd.server.services.IntegrateElectricDataService;

import static org.iii.esd.Constants.ROLE_FIELDADMIN;
import static org.iii.esd.Constants.ROLE_FIELDUSER;
import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.mongo.util.ModelHelper.asNonNull;

@Service
@Log4j2
public class AuthorizationHelper {
    @Autowired
    private IntegrateElectricDataService electricDataService;

    @Autowired
    private IntegrateRelationService relationService;

    @Autowired
    private IntegrateDataService dataService;

    @Autowired
    private IntegrateBidService bidService;

    @Autowired
    private QseService qseService;

    @Autowired
    private TxgService txgService;

    @Autowired
    private TxgFieldService resService;

    @Autowired
    private UserService userService;

    private static final Set<Long> SYS_AUTHOR = ImmutableSet.of(
            Long.parseLong(ROLE_SYSADMIN));

    private static final Set<Long> QSE_AUTHOR = ImmutableSet.of(
            Long.parseLong(ROLE_QSEADMIN),
            Long.parseLong(ROLE_QSEUSER));

    private static final Set<Long> TXG_AUTHOR = ImmutableSet.of(
            Long.parseLong(ROLE_SIADMIN),
            Long.parseLong(ROLE_SIUSER));


    private static final Set<Long> RES_AUTHOR = ImmutableSet.of(
            Long.parseLong(ROLE_FIELDADMIN),
            Long.parseLong(ROLE_FIELDUSER));

    public String getUserNameByAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    public UserProfile getUserProfileByAuthentication(Authentication authentication) throws WebException {
        String email = getUserNameByAuthentication(authentication);
        return userService.getByEmail(email);
    }

    public Long getRoleIdByAuthentication(Authentication authentication) throws WebException {
        UserProfile user = getUserProfileByAuthentication(authentication);
        return user.getRoleId();
    }

    public void checkTxgAuthorization(Authentication authentication, String txgId) throws WebException {
        UserProfile user = getUserProfileByAuthentication(authentication);
        Long roleId = user.getRoleId();

        if (RES_AUTHOR.contains(roleId)) {
            throw new WebException(Error.unauthorized);
        } else if (TXG_AUTHOR.contains(roleId)) {
            UserProfile.OrgId orgId = user.getOrgId();

            if (!Objects.isNull(orgId)
                    && !Objects.isNull(orgId.getId())
                    && !orgId.getId().equals(txgId)) {
                throw new WebException(Error.unauthorized);
            }
        } else if (QSE_AUTHOR.contains(roleId)) {
            UserProfile.OrgId orgId = user.getOrgId();
            Set<String> txgIds =
                    asNonNull(relationService.seekTxgProfilesFromQseId(orgId.getId()))
                            .stream()
                            .map(TxgProfile::getTxgId)
                            .collect(Collectors.toSet());
            if(!txgIds.contains(txgId)){
                throw new WebException(Error.unauthorized);
            }
        }
    }

    public boolean isSysAuthor(Authentication authentication) throws WebException {
        UserProfile user = getUserProfileByAuthentication(authentication);
        Long roleId = user.getRoleId();
        return SYS_AUTHOR.contains(roleId);
    }

    public boolean isQseAuthor(Authentication authentication) throws WebException {
        UserProfile user = getUserProfileByAuthentication(authentication);
        Long roleId = user.getRoleId();
        return QSE_AUTHOR.contains(roleId);
    }

    public boolean isTxgAuthor(Authentication authentication) throws WebException {
        UserProfile user = getUserProfileByAuthentication(authentication);
        Long roleId = user.getRoleId();
        return TXG_AUTHOR.contains(roleId);
    }

    public boolean isResAuthor(Authentication authentication) throws WebException {
        UserProfile user = getUserProfileByAuthentication(authentication);
        Long roleId = user.getRoleId();
        return RES_AUTHOR.contains(roleId);
    }

    public void checkResAuthorization(Authentication authentication, String id) throws WebException {
        UserProfile user = getUserProfileByAuthentication(authentication);
        Long roleId = user.getRoleId();

        if (RES_AUTHOR.contains(roleId)) {
            UserProfile.OrgId orgId = user.getOrgId();

            if (!Objects.isNull(orgId)
                    && !Objects.isNull(orgId.getId())
                    && !orgId.getId().equals(id)) {
                throw new WebException(Error.unauthorized);
            }
        } else if (TXG_AUTHOR.contains(roleId)) {
            UserProfile.OrgId orgId = user.getOrgId();
            Set<String> resIds =
                    asNonNull(relationService.seekTxgFieldProfilesFromTxgId(orgId.getId()))
                            .stream()
                            .map(TxgFieldProfile::getResId)
                            .collect(Collectors.toSet());

            if (!resIds.contains(id)) {
                throw new WebException(Error.unauthorized);
            }
        } else if (QSE_AUTHOR.contains(roleId)) {
            UserProfile.OrgId orgId = user.getOrgId();
            Set<String> resIds =
                    asNonNull(relationService.seekTxgProfilesFromQseId(orgId.getId()))
                            .stream()
                            .flatMap(txg -> asNonNull(relationService.seekTxgFieldProfilesFromTxgId(txg.getTxgId())).stream())
                            .map(TxgFieldProfile::getResId)
                            .collect(Collectors.toSet());
            if (!resIds.contains(id)) {
                throw new WebException(Error.unauthorized);
            }
        }
    }

    public UserOrg getUserOrg(UserProfile userEntity) {
        UserOrg userOrg;
        TxgFieldProfile res;
        TxgProfile txg;
        QseProfile qse;

        switch (userEntity.getOrgId().getType()) {
            case RES:
                res = resService.findByResId(userEntity.getOrgId().getId());
                txg = txgService.findByTxgId(res.getTxgId());
                qse = qseService.findByQseId(txg.getQseId());

                userOrg = UserOrg.builder()
                                 .qse(qse)
                                 .txg(txg)
                                 .res(res)
                                 .build();

                return userOrg;
            case TXG:
                txg = txgService.findByTxgId(userEntity.getOrgId().getId());
                qse = qseService.findByQseId(txg.getQseId());

                userOrg = UserOrg.builder()
                                 .qse(qse)
                                 .txg(txg)
                                 .build();

                return userOrg;
            case QSE:
                qse = qseService.findByQseId(userEntity.getOrgId().getId());

                userOrg = UserOrg.builder()
                                 .qse(qse)
                                 .build();

                return userOrg;
            default:
                throw new ApplicationException(Error.invalidParameter, userEntity.getOrgId().getType());
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class UserOrg {
        private QseProfile qse;
        private TxgProfile txg;
        private TxgFieldProfile res;
    }
}
