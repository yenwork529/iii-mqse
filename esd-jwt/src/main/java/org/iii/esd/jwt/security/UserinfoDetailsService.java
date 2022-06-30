package org.iii.esd.jwt.security;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.iii.esd.Constants;
import org.iii.esd.exception.ApplicationException;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.integrate.CompanyProfile;
import org.iii.esd.mongo.document.integrate.QseProfile;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.document.integrate.UserProfile;
import org.iii.esd.mongo.service.integrate.CompanyService;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.mongo.service.integrate.QseService;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.mongo.service.integrate.UserService;

import static java.util.stream.Collectors.toSet;

@Service
@Log4j2
public class UserinfoDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;
    @Autowired
    private IntegrateRelationService relationService;
    @Autowired
    private QseService qseService;
    @Autowired
    private TxgService txgService;
    @Autowired
    private TxgFieldService resService;
    @Autowired
    private CompanyService comService;

    @Override
    public UserinfoDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserProfile userEntity = userService.findByEmail(email);
        List<QseProfile> allQse = qseService.getQseList();

        if (Objects.isNull(userEntity)) {
            log.warn("User not found with email : {}", email);
            throw new UsernameNotFoundException("User not found with email : " + email);
        } else if (isInitialAdmin(userEntity, allQse)) {
            return getUserinfoDetails(userEntity);
        }

        CompanyProfile company = comService.findByCompanyId(userEntity.getCompanyId());
        userEntity.setCompany(company);

        setUserOrg(userEntity);

        return getUserinfoDetails(userEntity);
    }

    public void setUserOrg(UserProfile userEntity) {
        TxgFieldProfile res;
        TxgProfile txg;
        QseProfile qse;

        switch (userEntity.getOrgId().getType()) {
            case RES:
                res = resService.findByResId(userEntity.getOrgId().getId());
                txg = txgService.findByTxgId(res.getTxgId());
                qse = qseService.findByQseId(txg.getQseId());

                userEntity.setRes(res);
                userEntity.setTxg(txg);
                userEntity.setQse(qse);

                break;
            case TXG:
                txg = txgService.findByTxgId(userEntity.getOrgId().getId());
                qse = qseService.findByQseId(txg.getQseId());

                userEntity.setTxg(txg);
                userEntity.setQse(qse);

                break;
            case QSE:
                qse = qseService.findByQseId(userEntity.getOrgId().getId());

                userEntity.setQse(qse);

                break;
            default:
                throw new ApplicationException(Error.invalidParameter, userEntity.getOrgId().getType());
        }
    }

    private boolean isInitialAdmin(UserProfile userEntity, List<QseProfile> allQse) {
        return !Objects.isNull(userEntity.getRoleId())
                && userEntity.getRoleId().toString().equals(Constants.ROLE_SYSADMIN)
                && StringUtils.isEmpty(userEntity.getCompanyId())
                && Objects.isNull(userEntity.getOrgId())
                && CollectionUtils.isEmpty(allQse);
    }

    public UserinfoDetails loadUserById(Long id) throws UsernameNotFoundException {
        UserProfile user =
                userService.findById(id)
                           .orElseThrow(() ->
                                   new UsernameNotFoundException("User not found with id : " + id));

        return getUserinfoDetails(user);
    }

    private UserinfoDetails getUserinfoDetails(UserProfile userEntity) {
        Set<GrantedAuthority> authorities;

        authorities = Stream.of(userEntity.getRoleId())
                            .map(id -> new SimpleGrantedAuthority("ROLE_" + id))
                            .collect(toSet());

        return new UserinfoDetails(userEntity, authorities);
    }

}