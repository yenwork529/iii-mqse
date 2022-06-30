package org.iii.esd.mongo.document;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "AutomaticFrequencyControlMeasure")
@CompoundIndexes({
        @CompoundIndex(def = "{'afcId':1, 'timestamp':1, 'type':1}",
                name = "pk_automaticFrequencyControlMeasure",
                unique = true)
})
public class AutomaticFrequencyControlMeasure extends UuidDocument {

    /**
     * AFC頻率自動控制服務
     */
    @DBRef
    @Field("afcId")
    @JsonIgnore
    //	@JsonIgnoreProperties({ "updateTime", "createTime" })
    private AutomaticFrequencyControlProfile automaticFrequencyControlProfile;
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