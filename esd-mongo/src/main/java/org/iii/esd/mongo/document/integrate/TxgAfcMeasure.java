package org.iii.esd.mongo.document.integrate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.iii.esd.mongo.document.UuidDocument;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "TxgAfcMeasure")
@CompoundIndexes({
        @CompoundIndex(def = "{'TcgAfcId':1, 'timestamp':1, 'type':1}",
                name = "pk_TxgAfcMeasure",
                unique = true)
})

public class TxgAfcMeasure extends UuidDocument {

//    @DBRef
//    @Field("resId")
//    private TxgFieldProfile txgFieldProfile;

    private String resId;

    /**
     * AFC頻率自動控制服務
     */
//    @DBRef
//    @Field("TxgAfcId")
//    @JsonIgnore
//    //	@JsonIgnoreProperties({ "updateTime", "createTime" })
//    private TxgAfcProfile txgafcProfile;

    private String TxgAfcId;
    /**
     * AFC運轉日期時間
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;
    /**
     * 測量類型:
     * SPM(15分鐘平均執行率)
     * ASPM(年平均執行率)
     */
    private String type;
    /**
     * 資料更新時間
     */
    @LastModifiedDate
    @JsonIgnore
    private Date updateTime;
    /**
     * 資料建立時間
     */
    @JsonIgnore
    @CreatedDate
    private Date createTime;
    /**
     * 測量值(平均數)
     */
    private BigDecimal value;
    /**
     * 平均筆數
     */
    private Integer count;

}
