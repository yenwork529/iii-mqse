package org.iii.esd.initializer;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import org.iii.esd.config.DataConfiguration;
import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.exception.ApplicationException;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.CompanyProfile;
import org.iii.esd.mongo.document.integrate.QseProfile;
import org.iii.esd.mongo.document.integrate.TxgDeviceProfile;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.document.integrate.UserProfile;
import org.iii.esd.mongo.enums.DeviceType;
import org.iii.esd.mongo.enums.LoadType;
import org.iii.esd.mongo.service.integrate.CompanyService;
import org.iii.esd.mongo.service.integrate.QseService;
import org.iii.esd.mongo.service.integrate.TxgDeviceService;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.mongo.service.integrate.UserService;
import org.iii.esd.mongo.vo.data.setup.SetupData;

import static org.iii.esd.config.Constants.PROFILE_SERVER;
import static org.iii.esd.mongo.util.ModelHelper.asNonNull;

@Component
@Slf4j
public class Initializer {
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private CompanyService companyService;

    @Autowired
    private QseService qseService;

    @Autowired
    private TxgService txgService;

    @Autowired
    private TxgFieldService resService;

    @Autowired
    private TxgDeviceService deviceService;

    @Autowired
    private DataConfiguration dataConfig;

    @Autowired
    private UserService userService;

    public void initData(String env) {
        initCompany();
        initOrganization();

        if (PROFILE_SERVER.equals(env)) {
            initUser();
        }
    }

    private void initCompany() {
        asNonNull(dataConfig.getCompanyList())
                .forEach(this::saveCompany);
    }

    private void saveCompany(DataConfiguration.CompanyItem item) {
        CompanyProfile entity = CompanyProfile.builder()
                                              .companyId(item.getCompanyId())
                                              .name(item.getShortName())
                                              .fullName(item.getFullName())
                                              .phone(item.getPhone())
                                              .address(item.getAddress())
                                              .contractPerson(item.getContractPerson())
                                              .build()
                                              .initial();
        companyService.create(entity);
    }

    private void initOrganization() {
        asNonNull(dataConfig.getQseList())
                .forEach(this::saveQse);
    }

    private void saveQse(DataConfiguration.QseItem qseItem) {
        QseProfile entity = QseProfile.builder()
                                      .qseId(qseItem.getQseId())
                                      .qseCode(qseItem.getQseCode())
                                      .name(qseItem.getQseName())
                                      .companyId(qseItem.getCompanyId())
                                      .dnpUrl(qseItem.getDnpUrl())
                                      .vpnLanIp(qseItem.getVpnLanIp())
                                      .vpnWanIp(qseItem.getVpnWanIp())
                                      .lineToken(qseItem.getLineToken())
                                      .build()
                                      .initial();
        qseService.create(entity);

        asNonNull(qseItem.getTxgList())
                .forEach(txgItem -> saveTxg(txgItem, qseItem.getQseId()));
    }

    private void saveTxg(DataConfiguration.TxgItem txgItem, String qseId) {
        TxgProfile entity = TxgProfile.builder()
                                      .qseId(qseId)
                                      .txgId(txgItem.getTxgId())
                                      .txgCode(txgItem.getTxgCode())
                                      .name(txgItem.getTxgName())
                                      .companyId(txgItem.getCompanyId())
                                      .serviceType(txgItem.getServiceType())
                                      .efficiencyPrice(BigDecimal.valueOf(txgItem.getEfficiencyPrice()))
                                      .registerCapacity(BigDecimal.valueOf(txgItem.getRegisterCapacity()))
                                      .enableStatus(EnableStatus.enable)
                                      .lineToken(txgItem.getLineToken())
                                      .build()
                                      .initial();
        txgService.create(entity);

        asNonNull(txgItem.getResList())
                .forEach(resItem -> saveRes(resItem, txgItem.getTxgId()));
    }

    private void saveRes(DataConfiguration.ResItem resItem, String txgId) {
        TxgFieldProfile entity = TxgFieldProfile.builder()
                                                .resId(resItem.getResId())
                                                .companyId(resItem.getCompanyId())
                                                .name(resItem.getResName())
                                                .txgId(txgId)
                                                .resType(resItem.getResType())
                                                .resCode(resItem.getResCode())
                                                .tcUrl(resItem.getTcUrl())
                                                .accFactor(BigDecimal.valueOf(resItem.getAccFactor()))
                                                .registerCapacity(BigDecimal.valueOf(resItem.getRegisterCapacity()))
                                                .tcEnable(EnableStatus.enable)
                                                .devStatus(ConnectionStatus.Connected)
                                                .lineToken(resItem.getLineToken())
                                                .build()
                                                .initial();
        resService.create(entity);

        asNonNull(resItem.getDeviceList())
                .forEach(devItem -> saveDev(devItem, resItem.getResId()));
    }

    private void saveDev(DataConfiguration.DevItem devItem, String resId) {
        try {
            TxgDeviceProfile entity = TxgDeviceProfile.builder()
                                                      .id(devItem.getDeviceId())
                                                      .name(devItem.getDeviceName())
                                                      .resId(resId)
                                                      .deviceType(DeviceType.Meter)
                                                      .loadType(getLoadType(devItem.getLoadType()))
                                                      .connectionStatus(ConnectionStatus.Connected)
                                                      .setupData(
                                                              SetupData.builder()
                                                                       .ct(BigDecimal.valueOf(devItem.getCt()))
                                                                       .pt(BigDecimal.valueOf(devItem.getPt()))
                                                                       .ratedVoltage(BigDecimal.valueOf(or(devItem.getRatedVoltage(), 0.0)))
                                                                       .build())
                                                      .enableStatus(EnableStatus.enable)
                                                      .isMainLoad(true)
                                                      .isSync(true)
                                                      .build();
            deviceService.create(entity);
        } catch (WebException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
        }
    }

    private LoadType getLoadType(Integer loadType) {
        if (loadType == 1) {
            return LoadType.M1;
        } else if (loadType == 3) {
            return LoadType.M3;
        } else {
            return LoadType.Undefiend;
        }
    }

    private <T> T or(T target, T defaut) {
        return Optional.ofNullable(target).orElse(defaut);
    }

    private void initUser() {
        asNonNull(dataConfig.getUserList())
                .forEach(this::saveUser);
    }

    private void saveUser(DataConfiguration.UserItem userItem) {
        UserProfile entity = UserProfile.builder()
                                        .email(userItem.getEmail())
                                        .password(passwordEncoder.encode(userItem.getPassword()))
                                        .roleId(userItem.getRoleId())
                                        .phones(new String[]{})
                                        .companyId(userItem.getCompanyId())
                                        .name(userItem.getName())
                                        .retry(0)
                                        .noticeTypes(Collections.singleton(1L))
                                        .lineToken("")
                                        .orgId(buildOrgId(userItem))
                                        .enableStatus(EnableStatus.enable)
                                        .build()
                                        .initial();
        userService.create(entity);
    }

    private UserProfile.OrgId buildOrgId(DataConfiguration.UserItem userItem) {
        UserProfile.OrgType orgType;
        String orgId;

        if (!Objects.isNull(userItem.getQseId())) {
            orgType = UserProfile.OrgType.QSE;
            orgId = userItem.getQseId();
        } else if (!Objects.isNull(userItem.getTxgId())) {
            orgType = UserProfile.OrgType.TXG;
            orgId = userItem.getTxgId();
        } else if (!Objects.isNull(userItem.getResId())) {
            orgType = UserProfile.OrgType.RES;
            orgId = userItem.getResId();
        } else {
            throw new ApplicationException(Error.insertDataError, "Org type not supported with this user.");
        }

        return UserProfile.OrgId.builder()
                                .id(orgId)
                                .type(orgType)
                                .build();
    }
}
