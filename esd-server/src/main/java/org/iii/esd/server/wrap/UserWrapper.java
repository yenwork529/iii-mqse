package org.iii.esd.server.wrap;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import org.iii.esd.api.vo.OrgTree;
import org.iii.esd.api.vo.SiloUser;
import org.iii.esd.api.vo.integrate.User;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.mongo.document.DemandResponseProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.SiloCompanyProfile;
import org.iii.esd.mongo.document.SiloUserProfile;
import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.mongo.document.integrate.UserProfile;

public class UserWrapper {

    public static SiloUserProfile wrap(SiloUser siloUser) {
        return merge(new SiloUserProfile(), siloUser);
    }

    public static SiloUserProfile merge(SiloUserProfile siloUserProfile, SiloUser siloUser) {
        siloUserProfile.setId(siloUser.getId());
        siloUserProfile.setName(siloUser.getName());
        siloUserProfile.setEmail(siloUser.getEmail());
        siloUserProfile.setRoleIds(siloUser.getRoleIds());
        siloUserProfile.setEnableStatus(siloUser.getEnableStatus());
        if (!StringUtils.isBlank(siloUser.getPassword())) {
            siloUserProfile.setPassword(siloUser.getPassword());
        }
        if (siloUser.getCompanyId() != null) {
            siloUserProfile.setSiloCompanyProfile(new SiloCompanyProfile(siloUser.getCompanyId()));
        }
        if (siloUser.getAfcId() != null) {
            siloUserProfile.setAutomaticFrequencyControlProfile(new AutomaticFrequencyControlProfile(siloUser.getAfcId()));
        }
        if (siloUser.getDrId() != null) {
            siloUserProfile.setDemandResponseProfile(new DemandResponseProfile(siloUser.getDrId()));
        }
        if (siloUser.getSrId() != null) {
            siloUserProfile.setSpinReserveProfile(new SpinReserveProfile(siloUser.getSrId()));
        }
        if (siloUser.getFieldId() != null) {
            siloUserProfile.setFieldProfile(new FieldProfile(siloUser.getFieldId()));
        }
        siloUserProfile.setPhones(siloUser.getPhones());
        siloUserProfile.setLineToken(siloUser.getLineToken());
        siloUserProfile.setNoticeTypes(siloUser.getNoticeTypes());
        return siloUserProfile;
    }

    public static UserProfile wrapNew(User vo) {
        UserProfile entity = UserProfile.builder()
                                        .roleId(vo.getRoleId())
                                        .email(vo.getEmail())
                                        .orgId(buildUserOrg(vo.getUnit()))
                                        .name(vo.getName())
                                        .phones(vo.getPhones())
                                        .noticeTypes(vo.getNoticeTypes())
                                        .enableStatus(vo.getEnableStatus())
                                        .retry(0)
                                        .build();

        if (!StringUtils.isEmpty(vo.getPassword())) {
            entity.setPassword(vo.getPassword());
        }

        return entity;
    }

    private static UserProfile.OrgId buildUserOrg(OrgTree.Unit unit) {
        if (!Objects.isNull(unit)) {
            return UserProfile.OrgId.builder()
                                    .type(UserProfile.OrgType.ofName(unit.getUnitType().name()))
                                    .id(unit.getUnitId())
                                    .build();
        }

        return null;
    }

    public static User unwrapNew(UserProfile entity, OrgTree orgTree) {
        return User.builder()
                   .id(entity.getId())
                   .roleId(entity.getRoleId())
                   .email(entity.getEmail())
                   .companyId(entity.getCompanyId())
                   .unit(buildUserUnit(orgTree, entity.getOrgId()))
                   .name(entity.getName())
                   .phones(entity.getPhones())
                   .noticeTypes(entity.getNoticeTypes())
                   .enableStatus(entity.getEnableStatus())
                   .retry(entity.getRetry())
                   .lastLoginTime(entity.getLastLoginTime())
                   .build();
    }

    private static OrgTree.Unit buildUserUnit(OrgTree orgTree, UserProfile.OrgId orgId) {
        if (Objects.isNull(orgId)) {
            return null;
        }

        OrgTree.Unit defaultUnit = OrgTree.Unit.builder()
                                               .unitId(orgId.getId())
                                               .unitType(OrgTree.Type.ofName(orgId.getType().name()))
                                               .build();

        if (Objects.isNull(orgTree) || Objects.isNull(orgTree.getMyUnit())) {
            return defaultUnit;
        }

        return findUserUnit(orgTree.getMyUnit().toList(), orgId, defaultUnit);
    }

    private static OrgTree.Unit findUserUnit(List<OrgTree.Unit> units, UserProfile.OrgId orgId, OrgTree.Unit defaultUnit) {
        for (OrgTree.Unit unit : units) {
            if (checkUnitAndOrgId(unit, orgId)) {
                return unit;
            }
        }

        return defaultUnit;
    }

    private static boolean checkUnitAndOrgId(OrgTree.Unit unit, UserProfile.OrgId orgId) {
        return Objects.equals(unit.getUnitId(), orgId.getId())
                && Objects.equals(unit.getUnitType().name(), orgId.getType().name());
    }
}