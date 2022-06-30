package org.iii.esd.server.services;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.iii.esd.api.vo.OrgTree;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.exception.ApplicationException;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.SiloCompanyProfile;
import org.iii.esd.mongo.document.SiloUserProfile;
import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.mongo.document.integrate.QseProfile;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.document.integrate.UserProfile;
import org.iii.esd.mongo.enums.ResourceType;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.mongo.service.SiloUserService;
import org.iii.esd.mongo.service.SpinReserveService;
import org.iii.esd.mongo.service.integrate.IntegrateRelationInterface;
import org.iii.esd.mongo.service.integrate.QseService;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.mongo.service.integrate.UserService;
import org.iii.esd.utils.DatetimeUtils;

import static org.iii.esd.Constants.QSE_AUTHOR;
import static org.iii.esd.Constants.RES_AUTHOR;
import static org.iii.esd.Constants.SYS_AUTHOR;
import static org.iii.esd.Constants.TXG_AUTHOR;
import static org.iii.esd.mongo.util.ModelHelper.asNonNull;
import static org.iii.esd.server.utils.AuthorizationUtil.isQseLevel;
import static org.iii.esd.server.utils.AuthorizationUtil.isResLevel;
import static org.iii.esd.server.utils.AuthorizationUtil.isSysLevel;
import static org.iii.esd.server.utils.AuthorizationUtil.isTxgLevel;

@Service
public class OperatingService {

    @Autowired
    private SiloUserService siloUserService;

    @Autowired
    private SpinReserveService srService;

    @Autowired
    private FieldProfileService fldService;

    @Autowired
    private UserService userService;

    @Autowired
    private IntegrateRelationInterface integrateRelation;

    @Autowired
    private TxgFieldService txgFieldService;

    @Autowired
    private TxgService txgService;

    @Autowired
    private QseService qseService;

    public OrgTree buildOrgTreeFromUser(UserProfile user) throws WebException {
        long roleId = user.getRoleId();

        if (isResLevel(roleId)) {
            return buildResOrgTreeFromUser(user);
        } else if (isTxgLevel(roleId)) {
            return buildTxgOrgTreeFromUser(user);
        } else if (isSysLevel(roleId) || isQseLevel(roleId)) {
            return buildQseOrgTreeFromUser(user);
        } else {
            throw new WebException(Error.unauthorized);
        }
    }

    @Deprecated
    public OrgTree buildOrgTreeFromUser(SiloUserProfile user) throws WebException {
        long roleId = user.getRoleIds()
                          .stream()
                          .findFirst()
                          .orElseThrow();

        if (isResLevel(roleId)) {
            return buildResOrgTreeFromUser(user);
        } else if (isTxgLevel(roleId)) {
            return buildTxgOrgTreeFromUser(user);
        } else if (isSysLevel(roleId) || isQseLevel(roleId)) {
            return buildQseOrgTreeFromUser(user);
        } else {
            throw new WebException(Error.unauthorized);
        }
    }

    public OrgTree buildTopOrgTree() throws WebException {
        return integrateRelation.seekQseProfiles()
                                .stream()
                                .findFirst().map(this::buildQseOrgTree)
                                .orElseThrow(WebException.of(Error.noData, "qse"));
    }

    public OrgTree buildTopOrgTreeByDate(LocalDate localDate) throws WebException {
        return integrateRelation.seekQseProfiles()
                                .stream()
                                .findFirst().map(qse -> this.buildQseOrgTreeByDate(qse, localDate))
                                .orElseThrow(WebException.of(Error.noData, "qse"));
    }

    public OrgTree buildTopOrgTreeByQse(QseProfile qse) {
        return this.buildQseOrgTree(qse);
    }

    public OrgTree buildTopOrgTreeByQseAndDate(QseProfile qse, LocalDate localDate) {
        return this.buildQseOrgTreeByDate(qse, localDate);
    }

    public OrgTree filterOrgTreeByMyAuthor(OrgTree topOrgTree, UserProfile user) throws WebException {
        Long author = user.getRoleId();

        if (SYS_AUTHOR.contains(author) || QSE_AUTHOR.contains(author)) {
            return topOrgTree;
        } else if (TXG_AUTHOR.contains(author)) {
            TxgProfile txg = txgService.findByTxgId(user.getOrgId().getId());

            OrgTree.Unit myQse = topOrgTree.getMyUnit();
            OrgTree.Unit myTxg = topOrgTree.getMyUnit()
                                           .getSubUnits()
                                           .stream()
                                           .filter(t -> Objects.equals(t.getUnitId(), txg.getTxgId()))
                                           .findFirst()
                                           .orElseThrow(() -> new WebException(Error.internalServerError,
                                                   String.format("user's orgId(%s) is not found in txg ids.", user.getOrgId().getId())));

            return OrgTree.builder()
                          .myUnit(OrgTree.Unit.builder()
                                              .unitId(myQse.getUnitId())
                                              .unitType(myQse.getUnitType())
                                              .unitName(myQse.getUnitName())
                                              .unitCode(myQse.getUnitCode())
                                              .sysId(myQse.getSysId())
                                              .subUnits(Collections.singletonList(myTxg))
                                              .build()).build();
        } else if (RES_AUTHOR.contains(author)) {
            TxgFieldProfile res = txgFieldService.findByResId(user.getOrgId().getId());
            TxgProfile txg = txgService.findByTxgId(res.getTxgId());

            OrgTree.Unit myQse = topOrgTree.getMyUnit();
            OrgTree.Unit myTxg = topOrgTree.getMyUnit()
                                           .getSubUnits()
                                           .stream()
                                           .filter(t -> Objects.equals(t.getUnitId(), txg.getTxgId()))
                                           .findFirst()
                                           .orElseThrow(() -> new WebException(Error.internalServerError,
                                                   String.format("user's orgId(%s) is not found in txg ids.", user.getOrgId().getId())));
            OrgTree.Unit myRes = myTxg.getSubUnits()
                                      .stream()
                                      .filter(r -> Objects.equals(r.getUnitId(), res.getResId()))
                                      .findFirst()
                                      .orElseThrow(() -> new WebException(Error.internalServerError,
                                              String.format("user's orgId(%s) is not found in txg ids.", user.getOrgId().getId())));

            return OrgTree.builder()
                          .myUnit(OrgTree.Unit.builder()
                                              .unitId(myQse.getUnitId())
                                              .unitType(myQse.getUnitType())
                                              .unitName(myQse.getUnitName())
                                              .unitCode(myQse.getUnitCode())
                                              .sysId(myQse.getSysId())
                                              .subUnits(Collections.singletonList(
                                                      OrgTree.Unit.builder()
                                                                  .unitId(myTxg.getUnitId())
                                                                  .unitType(myTxg.getUnitType())
                                                                  .unitName(myTxg.getUnitName())
                                                                  .unitCode(myTxg.getUnitCode())
                                                                  .sysId(myTxg.getSysId())
                                                                  .subUnits(Collections.singletonList(myRes))
                                                                  .build())).build()).build();
        } else {
            throw new WebException(Error.internalServerError, String.format("user(%s) has no role id or not supported.", user.getEmail()));
        }
    }

    @Deprecated
    private OrgTree buildQseOrgTreeFromUser(SiloUserProfile user) {
        SiloCompanyProfile company = user.getSiloCompanyProfile();
        SpinReserveProfile txg = user.getSpinReserveProfile();
        List<FieldProfile> fields = fldService.findFieldProfileBySrId(txg.getId(), EnableStatus.enable);

        List<OrgTree.Unit> fieldUnits = fields.stream()
                                              .map(field -> OrgTree.Unit.builder()
                                                                        .unitType(OrgTree.Type.RES)
                                                                        .sysId(field.getId())
                                                                        .unitName(field.getName())
                                                                        .unitCode(field.getResCode())
                                                                        .resourceType(ResourceType.dr.getCode())
                                                                        .build())
                                              .collect(Collectors.toList());

        OrgTree.Unit txgUnit = OrgTree.Unit.builder()
                                           .unitType(OrgTree.Type.TXG)
                                           .sysId(txg.getId())
                                           .unitName(txg.getName())
                                           .unitCode(company.getTgCode())
                                           .serviceType(company.getServiceType())
                                           .subUnits(fieldUnits)
                                           .build();

        return OrgTree.builder()
                      .myUnit(OrgTree.Unit.builder()
                                          .unitType(OrgTree.Type.QSE)
                                          .sysId(company.getId())
                                          .unitCode(company.getQseCode())
                                          .unitName(company.getName())
                                          .subUnits(Collections.singletonList(txgUnit))
                                          .build())
                      .build();
    }

    private OrgTree buildQseOrgTreeFromUser(UserProfile user) throws WebException {
        if (!Objects.equals(UserProfile.OrgType.QSE, user.getOrgId().getType())) {
            throw new WebException(Error.unauthorized);
        }

        QseProfile qseProfile = integrateRelation.seekQseProfiles()
                                                 .stream()
                                                 .filter(qse -> qse.getQseId().equals(user.getOrgId().getId()))
                                                 .findFirst()
                                                 .orElseThrow();

        return buildQseOrgTree(qseProfile);
    }

    private OrgTree buildQseOrgTree(QseProfile qse) {
        return OrgTree.builder()
                      .myUnit(OrgTree.Unit.builder()
                                          .unitType(OrgTree.Type.QSE)
                                          .unitId(qse.getQseId())
                                          .sysId(qse.getId())
                                          .unitCode(qse.getQseCode())
                                          .unitName(qse.getName())
                                          .subUnits(buildTxgOrgTreeFromQseId(qse.getQseId()))
                                          .build())
                      .build();
    }

    private OrgTree buildQseOrgTreeByDate(QseProfile qse, LocalDate localDate) {
        return OrgTree.builder()
                      .myUnit(OrgTree.Unit.builder()
                                          .unitType(OrgTree.Type.QSE)
                                          .unitId(qse.getQseId())
                                          .sysId(qse.getId())
                                          .unitCode(qse.getQseCode())
                                          .unitName(qse.getName())
                                          .subUnits(buildTxgOrgTreeFromQseIdByDate(qse.getQseId(), localDate))
                                          .build())
                      .build();
    }

    private List<OrgTree.Unit> buildTxgOrgTreeFromQseId(String qseId) {
        List<TxgProfile> txgProfiles = asNonNull(integrateRelation.seekTxgProfilesFromQseId(qseId));

        return txgProfiles.stream()
                          .map(txg ->
                                  OrgTree.Unit.builder()
                                              .unitType(OrgTree.Type.TXG)
                                              .unitId(txg.getTxgId())
                                              .sysId(txg.getId())
                                              .unitName(txg.getName())
                                              .unitCode(txg.getTxgCode())
                                              .serviceType(txg.getServiceType())
                                              .subUnits(buildResOrgTreeFromTxgId(txg.getTxgId()))
                                              .build())
                          .collect(Collectors.toList());
    }

    private List<OrgTree.Unit> buildTxgOrgTreeFromQseIdByDate(String qseId, LocalDate localDate) {
        Date at = DatetimeUtils.toDate(localDate.atTime(23, 59, 59));
        List<TxgProfile> txgProfiles = asNonNull(integrateRelation.seekTxgProfilesFromQseIdAndDate(qseId, at));

        return txgProfiles.stream()
                          .map(txg ->
                                  OrgTree.Unit.builder()
                                              .unitType(OrgTree.Type.TXG)
                                              .unitId(txg.getTxgId())
                                              .sysId(txg.getId())
                                              .unitName(txg.getName())
                                              .unitCode(txg.getTxgCode())
                                              .serviceType(txg.getServiceType())
                                              .subUnits(buildResOrgTreeFromTxgIdByDate(txg.getTxgId(), at))
                                              .build())
                          .collect(Collectors.toList());
    }

    private List<OrgTree.Unit> buildResOrgTreeFromTxgId(String txgId) {
        List<TxgFieldProfile> resProfiles = asNonNull(integrateRelation.seekTxgFieldProfilesFromTxgId(txgId));

        return resProfiles.stream()
                          .map(res -> OrgTree.Unit.builder()
                                                  .unitId(res.getResId())
                                                  .unitType(OrgTree.Type.RES)
                                                  .sysId(res.getId())
                                                  .unitName(res.getName())
                                                  .unitCode(res.getResCode())
                                                  .resourceType(res.getResType())
                                                  .subUnits(Collections.emptyList())
                                                  .build())
                          .collect(Collectors.toList());
    }

    private List<OrgTree.Unit> buildResOrgTreeFromTxgIdByDate(String txgId, Date at) {
        List<TxgFieldProfile> resProfiles = asNonNull(integrateRelation.seekTxgFieldProfilesFromTxgIdAndDate(txgId, at));

        return resProfiles.stream()
                          .map(res -> OrgTree.Unit.builder()
                                                  .unitId(res.getResId())
                                                  .unitType(OrgTree.Type.RES)
                                                  .sysId(res.getId())
                                                  .unitName(res.getName())
                                                  .unitCode(res.getResCode())
                                                  .resourceType(res.getResType())
                                                  .subUnits(Collections.emptyList())
                                                  .build())
                          .collect(Collectors.toList());
    }

    @Deprecated
    private OrgTree buildTxgOrgTreeFromUser(SiloUserProfile user) {
        SiloCompanyProfile company = user.getSiloCompanyProfile();
        SpinReserveProfile txg = user.getSpinReserveProfile();
        List<FieldProfile> fields = fldService.findFieldProfileBySrId(txg.getId(), EnableStatus.enable);
        List<OrgTree.Unit> fieldUnits = fields.stream()
                                              .map(field -> OrgTree.Unit.builder()
                                                                        .unitType(OrgTree.Type.RES)
                                                                        .sysId(field.getId())
                                                                        .unitName(field.getName())
                                                                        .unitCode(field.getResCode())
                                                                        .resourceType(ResourceType.dr.getCode())
                                                                        .build())
                                              .collect(Collectors.toList());

        return OrgTree.builder()
                      .myUnit(OrgTree.Unit.builder()
                                          .unitType(OrgTree.Type.TXG)
                                          .sysId(txg.getId())
                                          .unitName(txg.getName())
                                          .unitCode(company.getTgCode())
                                          .serviceType(company.getServiceType())
                                          .subUnits(fieldUnits)
                                          .build())
                      .build();
    }

    private OrgTree buildTxgOrgTreeFromUser(UserProfile user) {
        TxgProfile txg = Optional.ofNullable(integrateRelation.seekTxgProfileFromTxgId(user.getOrgId().getId()))
                                 .orElseThrow(ApplicationException.ofNoData(user.getOrgId().getId()));

        List<TxgFieldProfile> resList = asNonNull(integrateRelation.seekTxgFieldProfilesFromTxgId(txg.getTxgId()));

        List<OrgTree.Unit> fieldUnits = resList.stream()
                                               .map(res -> OrgTree.Unit.builder()
                                                                       .unitType(OrgTree.Type.RES)
                                                                       .unitId(res.getResId())
                                                                       .sysId(res.getId())
                                                                       .unitName(res.getName())
                                                                       .unitCode(res.getResCode())
                                                                       .resourceType(res.getResType())
                                                                       .build())
                                               .collect(Collectors.toList());

        return OrgTree.builder()
                      .myUnit(OrgTree.Unit.builder()
                                          .unitType(OrgTree.Type.TXG)
                                          .sysId(txg.getId())
                                          .unitId(txg.getTxgId())
                                          .unitName(txg.getName())
                                          .unitCode(txg.getTxgCode())
                                          .serviceType(txg.getServiceType())
                                          .subUnits(fieldUnits)
                                          .build())
                      .build();
    }

    @Deprecated
    private OrgTree buildResOrgTreeFromUser(SiloUserProfile user) {
        FieldProfile field = user.getFieldProfile();

        return OrgTree.builder()
                      .myUnit(OrgTree.Unit.builder()
                                          .unitType(OrgTree.Type.RES)
                                          .sysId(field.getId())
                                          .unitName(field.getName())
                                          .unitCode(field.getResCode())
                                          .resourceType(ResourceType.dr.getCode())
                                          .build())
                      .build();
    }

    private OrgTree buildResOrgTreeFromUser(UserProfile user) throws WebException {
        TxgFieldProfile res = txgFieldService.getByResId(user.getOrgId().getId());
        return OrgTree.builder()
                      .myUnit(OrgTree.Unit.builder()
                                          .unitType(OrgTree.Type.RES)
                                          .unitId(res.getResId())
                                          .sysId(res.getId())
                                          .unitName(res.getName())
                                          .unitCode(res.getResCode())
                                          .resourceType(res.getResType())
                                          .build())
                      .build();
    }
}
