package org.iii.esd.mongo.document;

import java.util.Date;
import java.util.Set;
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
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.integrate.TxgProfile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "SiloUserProfile")
public class SiloUserProfile extends SequenceDocument {

    /**
     * username
     */
    private String name;

    /**
     * login id
     */
    @Indexed(unique = true)
    private String email;

    private String password;

    /**
     * TransactionGroup
     */
    @DBRef
    @Field("txgId")
    private TxgProfile txgProfile;
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
    @JsonIgnore
    private AutomaticFrequencyControlProfile automaticFrequencyControlProfile;
    /**
     * DR需量反應
     */
    @DBRef
    @Field("drId")
    @JsonIgnore
    private DemandResponseProfile demandResponseProfile;
    /**
     * SP即時備轉容量輔助服務
     */
    @DBRef
    @Field("srId")
    @JsonIgnore
    private SpinReserveProfile spinReserveProfile;
    /**
     * 所屬場域
     */
    @DBRef
    @Field("fieldId")
    @JsonIgnore
    private FieldProfile fieldProfile;
    /**
     * 啟用狀態
     */
    @Enumerated(EnumType.STRING)
    private EnableStatus enableStatus;
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
     * roleIds
     */
    private Set<Long> roleIds;
    /**
     * 聯絡電話(as a callee for spin reserve notification)
     */
    private String[] phones;
    /**
     *電話通知錄音檔(as a content for spin reserve notification)
     */
    //	private String audioURI;
    /**
     * line 憑證
     */
    private String lineToken;
    /**
     * 通知類型
     */
    private Set<Long> noticeTypes;
    /**
     * 登入錯誤次數
     */
    private Integer retry;
    /**
     * 最後登入時間
     */
    private Date lastLoginTime;
    /**
     * 忘記密碼key
     */
    private String reset;


    public SiloUserProfile(Long id) {
        super(id);
    }

    public SiloUserProfile(Long companyId, Long fieldId, Long afcId, Long drId, Long srId) {
        this.siloCompanyProfile = companyId != null ? new SiloCompanyProfile(companyId) : null;
        this.fieldProfile = fieldId != null ? new FieldProfile(fieldId) : null;
        this.automaticFrequencyControlProfile = afcId != null ? new AutomaticFrequencyControlProfile(afcId) : null;
        this.demandResponseProfile = drId != null ? new DemandResponseProfile(drId) : null;
        this.spinReserveProfile = srId != null ? new SpinReserveProfile(srId) : null;
    }

    public SiloUserProfile(String name, String email, String password, EnableStatus enableStatus, Long companyId, Long fieldId, Long afcId,
            Long drId, Long srId, Set<Long> roleIds) {
        this(companyId, fieldId, afcId, drId, srId);
        this.name = name;
        this.email = email;
        this.password = password;
        this.enableStatus = enableStatus;
        this.roleIds = roleIds;
    }

}