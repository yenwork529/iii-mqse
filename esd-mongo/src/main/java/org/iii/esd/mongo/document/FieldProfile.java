package org.iii.esd.mongo.document;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.enums.TouType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "FieldProfile")
@Deprecated
public class FieldProfile extends SequenceDocument {

    /**
     * 場域名稱
     */
    private String name;
    /**
     * 資源代碼
     */
    private Integer resCode;
    /**
     * 資料更新時間
     */
    @LastModifiedDate
    private Date updateTime;
    /**
     * 資料建立時間
     */
    private Date createTime;
    /**
     * 調度策略
     */
    @DBRef
    @Field("policyId")
    private PolicyProfile policyProfile;
    /**
     * 公司
     */
    @DBRef
    @Field("companyId")
    private SiloCompanyProfile siloCompanyProfile;
    /**
     * AFC調頻備轉輔助服務(儲能自動頻率控制)
     */
    @DBRef
    @Field("afcId")
    private AutomaticFrequencyControlProfile automaticFrequencyControlProfile;
    /**
     * DR需量反應
     */
    @DBRef
    @Field("drId")
    private DemandResponseProfile demandResponseProfile;
    /**
     * SP即時備轉容量輔助服務
     */
    @DBRef
    @Field("srId")
    private SpinReserveProfile spinReserveProfile;
    /**
     * SR遙測場域回傳index
     */
    private Integer srIndex;
    /**
     * 電價類型
     */
    @Enumerated(EnumType.STRING)
    private TouType touType;
    /**
     * 最佳契約容量TYOD(調度用)
     */
    private Integer tyod;
    /**
     * 簽約契約容量(效益計算用)
     */
    private Integer tyodc;
    /**
     * 當月最大需量TRHD
     */
    private Integer trhd;
    /**
     * 原始契約容量
     */
    private Integer oyod;
    /**
     * 是否使用電池保留量
     */
    private Boolean isReserve = true;
    /**
     * 調度目標類型(0:TRHD 1:TYOD)
     */
    private Integer targetType = 1;
    /**
     * 控制頻率(秒)
     */
    private Integer frequency = 180;
    /**
     * 執行Delay(秒)
     */
    private Integer delay = 3;
    /**
     * 場域對應氣象站ID，可以參考esd-thirdparty的config.yml
     */
    private String stationId;
    /**
     * ThinClient 啟用狀態
     */
    @Enumerated(EnumType.STRING)
    private EnableStatus tcEnable;
    /**
     * ThinClient IP Address
     */
    private String tcIp;
    /**
     * ThinClient資料最後上傳時間
     */
    private Date tcLastUploadTime;
    /**
     * 設備連線狀態
     */
    @Enumerated(EnumType.STRING)
    private ConnectionStatus devStatus;

    /**
     * 預測修正時間
     */
    private Date forecastFixTime;
    /**
     * 重新排程時間
     */
    private Date rescheduleTime;
    /**
     * 場域總卸載量kW(包含電池、發電機、可控設備)
     */
    private Integer unload;
    /**
     * 場域基準線
     */
    private BigDecimal base;
    /**
     * 降載目標
     */
    private BigDecimal target;
    /**
     * 是否同步
     */
    private Boolean isSync;
    /**
     * 是否要重新排程
     */
    private Boolean isNeedReschedule;
    /**
     * line 憑證
     */
    private String lineToken;

    private Boolean onlyOne;

    /**
     * 8/23
     * SR/SUP新增欄位
     */
  
    /**
     * 場域誤差因子
     */
    private  BigDecimal accFactor;


    public FieldProfile(Long id) {
        super(id);
    }

    public FieldProfile(Long companyId, Long afcId, Long drId, Long srId, Long id, Integer resCode, EnableStatus tcEnable) {
        this(id);
        this.siloCompanyProfile = companyId != null ? new SiloCompanyProfile(companyId) : null;
        this.automaticFrequencyControlProfile = afcId != null ? new AutomaticFrequencyControlProfile(afcId) : null;
        this.demandResponseProfile = drId != null ? new DemandResponseProfile(drId) : null;
        this.spinReserveProfile = srId != null ? new SpinReserveProfile(srId) : null;
        this.tcEnable = tcEnable;
        this.resCode = resCode;
    }

    public FieldProfile(Long companyId, Long afcId, Long drId, Long srId, Long id, EnableStatus tcEnable) {
        this(companyId, afcId, drId, srId, id, null, tcEnable);
    }

    public FieldProfile(Long companyId, Long afcId, Long drId, Long srId, EnableStatus tcEnable) {
        this(companyId, afcId, drId, srId, null, tcEnable);
    }

    public static FieldProfile createInstance(Long id) {
        if (id == null) {
            return new FieldProfile(0L);
        } else {
            return new FieldProfile(id);
        }
    }

    public void setNull() {
        this.isReserve = null;
        this.targetType = null;
        this.frequency = null;
        this.delay = null;
    }

    public ConnectionStatus getDevStatus() {
        return devStatus != null ? devStatus : ConnectionStatus.Init;
    }

}