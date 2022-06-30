package org.iii.esd.mongo.document.integrate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.Hash32;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "TxgProfile")
public class TxgProfile extends CustomizedDocument<TxgProfile> {

    public final static long SR_NOTICE_LEAD_TIME = 1000L * 60 * 10;
    public final static long SUP_NOTICE_LEAD_TIME = 1000L * 60 * 30;
    public final static long BASELINE_SAMPLE_COUNTS = 5;
    public final static long BASELINE_BEGIN_LEAD_TIME = 1000L * 60 * (1); // + BASELINE_SAMPLE_COUNTS);

    public static final String[] NO_UPDATE_PROPERTIES = { "id", "createTime" };
    public final static Integer SERVICE_AFC = 1;
    public final static Integer SERVICE_DREG = 2;
    public final static Integer SERVICE_SREG = 3;
    public final static Integer SERVICE_SR = 4;
    public final static Integer SERVICE_SUP = 5;
    public final static Integer SERVICE_EDREG = 6;
    public final static Integer RESOURCE_DR = 1;
    public final static Integer RESOURCE_CGEN = 2;
    public final static Integer RESOURCE_UGEN = 3;
    public final static Integer RESOURCE_GESS = 4;

    public static synchronized TxgProfile getInstanceByCodeAndId(Integer code, String id) {
        return new TxgProfile(code, id);
    }

    public Boolean isSrServiceType() {
        return this.serviceType == SERVICE_SR;
    }

    public Boolean isSupServiceType() {
        return this.serviceType == SERVICE_SUP;
    }

    public Boolean isSrupServiceType() {
        return this.serviceType == SERVICE_SR || this.serviceType == SERVICE_SUP;
    }

    public Boolean isDregServiceType() {
        return this.serviceType == SERVICE_DREG;
    }

    @Indexed(unique = true)
    private String txgId;

    private String companyId;

    private String qseId;

    private Integer txgCode;

    // AFC("AFC", 1),
    // dReg("dreg", 2),
    // sReg("sreg", 3),
    // SR("sr", 4),
    // SUP("sup", 5),
    // E-dReg("edreg", 6),
    // NOT_SUPPORTED("NOT_SUPPORTED", -1);
    private Integer serviceType;

    private String name;

    private Date tcLastUploadTime;

    /**
     * 競標契約容量( kW)
     */
    private BigDecimal registerCapacity;

    /**
     * line 憑證
     */
    private String lineToken;

    /**
     * callback url
     */
    private String callbackURL;

    /**
     * 效能價格($/MW)
     */
    private BigDecimal efficiencyPrice;

    /**
     * Latest dispatch date time and parameter.
     */
    private Date noticeTime;

    private BigDecimal clipKw;

    private Date startTime;

    private Date endTime;

    public TxgProfile(Integer code, String id) {
        super(Hash32.toLong(id));

        this.txgCode = code;
        this.txgId = id;
    }

    public String registerCapacityToMW() {
        if (Objects.isNull(this.registerCapacity)) {
            return "";
        }

        double f = this.registerCapacity.divide(BigDecimal.valueOf(1000.0), RoundingMode.HALF_UP).doubleValue();
        return Double.toString(f);
    }

    @Override
    public TxgProfile buildSequenceId() {
        if (StringUtils.isNotEmpty(this.txgId)) {
            super.setId(Hash32.toLong(this.txgId));
        }

        return this;
    }

    @Override
    public String getIdentityProperty() {
        return "txgId";
    }

    @Enumerated(EnumType.STRING)
    private EnableStatus enableStatus;

    public String getBidContractCapacityString() {
        return this.registerCapacity.divide(BigDecimal.valueOf(1000.0), 2, RoundingMode.HALF_UP)
                .toString();
    }

    public String ServiceTypeString() {
        if (serviceType == SERVICE_SR) {
            return "SR";
        }
        if (serviceType == SERVICE_SUP) {
            return "SUP";
        }
        if (serviceType == SERVICE_DREG) {
            return "DREG";
        }
        return "";
    }
}
