package org.iii.esd.jwt.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.mongo.document.DemandResponseProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.SiloCompanyProfile;
import org.iii.esd.mongo.document.SiloUserProfile;
import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.document.integrate.UserProfile;

import static org.iii.esd.mongo.document.integrate.UserProfile.OrgType.QSE;
import static org.iii.esd.mongo.document.integrate.UserProfile.OrgType.RES;

@Getter
@Setter
@Log4j2
public class UserinfoDetails extends User {

    private static final long serialVersionUID = -4654225257568553647L;

    private Long userId;

    private String userName;

    private String password;

    private Long siloCompanyId;

    private Long siloFieldId;

    private Long siloAfcId;

    private Long siloDrId;

    private Long siloSrId;

    private Set<Long> roles;

    private String companyId;

    private String qseId;

    private String txgId;

    private String resId;

    private Integer qseCode;

    private Integer tgCode;

    private Integer resCode;

    private Integer serviceType;

    private Integer resourceType;

    public UserinfoDetails(UserProfile user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getEmail(), user.getPassword(), EnableStatus.isEnabled(user.getEnableStatus()), true, true, true, authorities);
        this.userId = user.getId();
        this.userName = user.getEmail();
        this.password = user.getPassword();
        this.roles = Collections.singleton(user.getRoleId());
        this.companyId = user.getCompanyId();
        this.qseId = user.getQse().getQseId();
        this.qseCode = user.getQse().getQseCode();

        if (QSE == user.getOrgId().getType()) {
            return;
        } else {
            this.txgId = user.getTxg().getTxgId();
            this.tgCode = user.getTxg().getTxgCode();
            this.serviceType = user.getTxg().getServiceType();
        }

        if (RES == user.getOrgId().getType()) {
            this.resId = user.getRes().getResId();
            this.resCode = user.getRes().getResCode();
            this.resourceType = user.getRes().getResType();
        }
    }

    public UserinfoDetails(SiloUserProfile userProfile, Collection<? extends GrantedAuthority> authorities) {
        super(userProfile.getEmail(), userProfile.getPassword(), EnableStatus.enable.equals(userProfile.getEnableStatus()), true, true,
                true, authorities);
        this.userId = userProfile.getId();
        this.userName = userProfile.getEmail();
        this.password = userProfile.getPassword();
        this.roles = userProfile.getRoleIds();
        SiloCompanyProfile siloCompanyProfile = userProfile.getSiloCompanyProfile();
        if (siloCompanyProfile != null) {
            this.siloCompanyId = siloCompanyProfile.getId();
            this.qseCode = siloCompanyProfile.getQseCode();
            this.tgCode = siloCompanyProfile.getTgCode();
            this.serviceType = siloCompanyProfile.getServiceType();
        }
        FieldProfile fieldProfile = userProfile.getFieldProfile();
        if (fieldProfile != null) {
            this.siloFieldId = fieldProfile.getId();
            this.resourceType = TxgProfile.RESOURCE_DR;
        }
        AutomaticFrequencyControlProfile automaticFrequencyControlProfile = userProfile.getAutomaticFrequencyControlProfile();
        if (automaticFrequencyControlProfile != null) {
            this.siloAfcId = automaticFrequencyControlProfile.getId();
        }
        DemandResponseProfile demandResponseProfile = userProfile.getDemandResponseProfile();
        if (demandResponseProfile != null) {
            this.siloDrId = demandResponseProfile.getId();
        }
        SpinReserveProfile spinReserveProfile = userProfile.getSpinReserveProfile();
        if (spinReserveProfile != null) {
            this.siloSrId = spinReserveProfile.getId();
        }
    }

}