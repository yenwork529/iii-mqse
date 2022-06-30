package org.iii.esd.mongo.document.integrate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.SequenceDocument;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "TxgAfcProfile")
public class TxgAfcProfile extends SequenceDocument {

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
//    @DBRef
//    @Field("companyId")
//    @JsonIgnore
//    private TxgCompanyProfile txgCompanyProfile;

    private String companyId;


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

    public TxgAfcProfile(Long id) {
        super(id);
    }

    /**
     * 變更關聯性出現error
     */

//    public TxgAfcProfile(Long companyId, EnableStatus enableStatus) {
//        this(null, companyId, enableStatus);
//    }

//    public TxgAfcProfile(Long id, Long companyId, EnableStatus enableStatus) {
//        this(id);
//        this.txgCompanyProfile = companyId != null ? new TxgCompanyProfile(companyId) : null;
//        this.enableStatus = enableStatus;
//    }

    /**
     * DNP 位址
     */
    private String dnpURL;

}
