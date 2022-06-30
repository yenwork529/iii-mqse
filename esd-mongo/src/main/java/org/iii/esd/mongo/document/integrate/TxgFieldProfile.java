package org.iii.esd.mongo.document.integrate;

import lombok.*;

import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.enums.TouType;
import org.iii.esd.mongo.document.*;

import org.apache.commons.lang3.StringUtils;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "TxgFieldProfile")
public class TxgFieldProfile extends CustomizedDocument<TxgFieldProfile> {

    public static synchronized TxgFieldProfile getInstanceByCodeAndId(Integer code, String id) {
        return new TxgFieldProfile(code, id);
    }

    public static final int DEFAULT_RES_TYPE = 1;

    @Indexed(unique = true)
    private String resId;

    private String companyId;

    private String txgId;

    private Integer resCode;

    private Integer resType;

    private String name;

    private Date startTime, endTime;

    private String tcUrl;

    /**
     * ThinClient資料最後上傳時間
     */
    private Date tcLastUploadTime;

    /**
     * ThinClient 啟用狀態
     */
    @Enumerated(EnumType.STRING)
    private EnableStatus tcEnable;

    /**
     * 設備連線狀態
     */
    @Enumerated(EnumType.STRING)
    private ConnectionStatus devStatus;

    /**
     * line 憑證
     */
    private String lineToken;

    /**
     * 場域誤差因子
     */
    private BigDecimal accFactor;

    private BigDecimal registerCapacity;

    @Enumerated(EnumType.STRING)
    private EnableStatus enableStatus;

    @Enumerated(EnumType.STRING)
    private ConnectionStatus tcStatus;

    public TxgFieldProfile(Integer code, String id) {
        super(Hash32.toLong(id));

        this.resCode = code;
        this.resId = id;
    }

    public ConnectionStatus getDevStatus() {
        return devStatus != null ? devStatus : ConnectionStatus.Init;
    }

    @Override
    public TxgFieldProfile buildSequenceId() {
        if (StringUtils.isNotEmpty(this.resId)) {
            super.setId(Hash32.toLong(this.resId));
        }

        return this;
    }

    @Override
    public String getIdentityProperty() {
        return "resId";
    }

    public Boolean isDr(){
        return this.resType == TxgProfile.RESOURCE_DR;
    }

    public Boolean isGess(){
        return this.resType == TxgProfile.RESOURCE_GESS;
    }
}
