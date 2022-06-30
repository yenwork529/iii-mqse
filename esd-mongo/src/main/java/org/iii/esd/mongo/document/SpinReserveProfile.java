package org.iii.esd.mongo.document;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import org.iii.esd.enums.EnableStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "SpinReserveProfile")
@Deprecated
public class SpinReserveProfile extends SequenceDocument {

    /**
     * 即時備轉輔助服務名稱
     */
    private String name;
    /**
     * 資料更新時間
     */
    @LastModifiedDate
    private Date updateTime;
    /**
     * 資料建立時間
     */
    private Date createTime;

    private Date tcLastUploadTime;
    /**
     * 公司
     */
    @DBRef
    @Field("companyId")
    @JsonIgnore
    private SiloCompanyProfile siloCompanyProfile;
    /**
     * DNP 位址
     */
    private String dnpURL;
    /**
     * 啟用狀態
     */
    @Enumerated(EnumType.STRING)
    private EnableStatus enableStatus;
    /**
     * 競標契約容量( kW)
     */
    private Integer bidContractCapacity;
    /**
     * 通知時間
     */
    private Date noticeTime;
    /**
     * 抑低開始時間
     */
    private Date startTime;
    /**
     * 抑低結束時間
     */
    private Date endTime;
    /**
     * 抑低量( kW)
     */
    private Integer clipKW;
    /**
     * 電能費(元/ kWh)
     */
    private BigDecimal energyPrice;
    /**
     * line 憑證
     */
    private String lineToken;
    /**
     * callback url
     */
    private String callbackURL;

    /**
     * M1到t2的累積用電量(KWH)
     */
//    private BigDecimal powerM1t2;

    /**
     * M1到T3的累積用電量(KWH)
     */
//    private BigDecimal energyT3;

    /**
     * M1到T5的累積用電量(KWH)
     */
//    private BigDecimal energyT5;

    /**
     * 8/23
     * SR/SUP新增欄位
     */
    /**
     * 效能價格($/MW)
     */

    private BigDecimal efficacyPrice;

    public SpinReserveProfile(Long id) {
        super(id);
    }

    public SpinReserveProfile(Long companyId, EnableStatus enableStatus) {
        this(null, companyId, enableStatus);
    }

    public SpinReserveProfile(Long id, Long companyId, EnableStatus enableStatus) {
        this(id);
        this.siloCompanyProfile = companyId != null ? new SiloCompanyProfile(companyId) : null;
        this.enableStatus = enableStatus;
    }

    public String getBidContractCapacityString(){
        // int c = this.bidContractCapacity * 10;
        Double f = ((double)this.bidContractCapacity) / 1000.0;
        return f.toString();
    }

}