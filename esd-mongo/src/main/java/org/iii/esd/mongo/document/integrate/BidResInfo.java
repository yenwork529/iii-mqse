package org.iii.esd.mongo.document.integrate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.iii.esd.mongo.document.UuidDocument;

import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "BidResInfo")
public class BidResInfo extends UuidDocument {

    private String resId;

    /**
     * SP即時備轉參與日期時間
     */
	// timestamp 改為 Date 以相容原有程式, Gem, 2021/11/09
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;
    private Long timeticks;

    /**
     * 資料建立時間
     */
    @JsonIgnore
    private Date createTime = new Date();

    /**
     * 得標容量(MW)
     */
    private BigDecimal awardedCapacity;

    /**
     * 尖峰保證容量(MW)
     */
    private BigDecimal ppaCapacity;

    /**
     * 可調度容量(MW)
     */
    private BigDecimal capacity;

    public BidResInfo(String resId, Date dt){
        this.resId = resId;
        this.dt = dt;

        initial();
    }

    @BsonIgnore
    @Transient
    private Date dt;

    public BidResInfo initial(){
        long hour = dt.getTime() / 1000 / 60 / 60;
        this.timeticks = hour * 1000 * 60 * 60;
        this.timestamp = new Date(this.timeticks);
        super.id = resId + String.format("_%d", hour);

        return this;
    }
}
