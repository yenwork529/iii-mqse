package org.iii.esd.mongo.document;

import java.util.Date;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import org.iii.esd.enums.EnableStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "AutomaticFrequencyControlProfile")
public class AutomaticFrequencyControlProfile extends SequenceDocument {

    /**
     * 調頻備轉輔助服務(儲能自動頻率控制)名稱
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
    @CreatedDate
    private Date createTime;
    /**
     * 公司
     */
    @DBRef
    @Field("companyId")
    @JsonIgnore
    private SiloCompanyProfile siloCompanyProfile;
    /**
     * 啟用狀態
     */
    @Enumerated(EnumType.STRING)
    private EnableStatus enableStatus;
    /**
     * ThinClient資料最後上傳時間
     */
    private Date tcLastUploadTime;
    /**
     * line 憑證
     */
    private String lineToken;
    /**
     * 補值url
     */
    private String posturl;

    public AutomaticFrequencyControlProfile(Long id) {
        super(id);
    }

    public AutomaticFrequencyControlProfile(Long companyId, EnableStatus enableStatus) {
        this(null, companyId, enableStatus);
    }

    public AutomaticFrequencyControlProfile(Long id, Long companyId, EnableStatus enableStatus) {
        this(id);
        this.siloCompanyProfile = companyId != null ? new SiloCompanyProfile(companyId) : null;
        this.enableStatus = enableStatus;
    }

    /**
     * DNP 位址
     */
    private String dnpURL;

}