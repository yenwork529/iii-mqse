package org.iii.esd.config;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "data")
@Getter
@Setter
@ToString
public class DataConfiguration {

    private List<CompanyItem> companyList;
    private List<QseItem> qseList;
    private List<UserItem> userList;

    @Getter
    @Setter
    @ToString
    public static class CompanyItem{
        private String companyId;
        private String shortName;
        private String fullName;
        private String address;
        private String phone;
        private String contractPerson;
    }

    @Getter
    @Setter
    @ToString
    public static class QseItem{
        private String qseId;
        private Integer qseCode;
        private String qseName;
        private String companyId;
        private String dnpUrl;
        private String vpnLanIp;
        private String vpnWanIp;
        private String lineToken;
        private List<TxgItem> txgList;
    }

    @Getter
    @Setter
    @ToString
    public static class TxgItem{
        private String txgId;
        private Integer txgCode;
        private String txgName;
        private String companyId;
        private Double registerCapacity;
        private Double efficiencyPrice;
        private Integer serviceType;
        private String lineToken;
        private List<ResItem> resList;
    }

    @Getter
    @Setter
    @ToString
    public static class ResItem{
        private String resId;
        private Integer resCode;
        private String resName;
        private Integer resType;
        private String companyId;
        private Double accFactor;
        private Double registerCapacity;
        private String tcUrl;
        private String lineToken;
        private List<DevItem> deviceList;
    }

    @Getter
    @Setter
    @ToString
    public static class DevItem{
        private String deviceId;
        private String deviceName;
        private Integer loadType;
        private Integer ct;
        private Integer pt;
        private Double ratedVoltage;
    }

    @Getter
    @Setter
    @ToString
    public static class UserItem {
        private String email;
        private String name;
        private String password;
        private String companyId;
        private String qseId;
        private String txgId;
        private String resId;
        private Long roleId;
    }
}
