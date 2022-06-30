package org.iii.esd.mongo.document;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "SiloCompanyProfile")
public class SiloCompanyProfile extends SequenceDocument {

    /**
     * 公司名稱
     */
    private String name;

    /**
     * 合格交易商代碼
     */
    private Integer qseCode;

    /**
     * 交易群組代碼
     */
    private Integer tgCode;

    /**
     * 服務類型
     */
    private Integer serviceType;

    /**
     * DNP 位址
     */
    private String dnpURL;

    /**
     * callback url
     */
    private String callbackURL;

    /**
     * 資料更新時間
     */
    @LastModifiedDate
    private Date updateTime;
    /**
     * 資料建立時間
     */
    private Date createTime;

    public SiloCompanyProfile(Long id) {
        super(id);
    }

}
