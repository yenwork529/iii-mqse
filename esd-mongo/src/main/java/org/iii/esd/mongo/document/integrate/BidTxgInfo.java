package org.iii.esd.mongo.document.integrate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import org.iii.esd.enums.OperatorStatus;
import org.iii.esd.mongo.document.UuidDocument;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "BidTxgInfo")

public class BidTxgInfo extends UuidDocument {

    private String txgId;

    /**
     * SP即時備轉參與日期時間
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private long timeticks;

    private Date timestamp;

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
    private Date createTime = new Date();

    /**
     * 得標容量(MW)
     */
    private BigDecimal awardedCapacity;

    /**
     * 運轉狀態
     */
    private OperatorStatus operatorStatus;

    /**
     * 尖峰保證容量電能費價格(NT$/MWh)
     */
    private BigDecimal ppaEnergyPrice;

    /**
     * 尖峰保證容量(MW)
     */
    private BigDecimal ppaCapacity;

    /**
     * 競標資訊
     */

    /**
     * 電能費報價(NT$/MWh)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal energyPrice;

    /**
     * 可調度容量(MW)
     */
    private BigDecimal capacity;

    /**
     * 容量費單價(NT/MW)
     */
    private BigDecimal price;

    private Boolean abandon;

    private Date abandonFrom;

    public BidTxgInfo(String txgId, Date dt) {
        this.txgId = txgId;
        this.dt = dt;
    }

    @BsonIgnore
    @Transient
    private Date dt;

    public BidTxgInfo initial() {
        long hour = dt.getTime() / 1000 / 60 / 60;
        this.timeticks = hour * 1000 * 60 * 60;
        this.timestamp = new Date(this.timeticks);
        super.id = txgId + String.format("_%d", hour);

        return this;
    }

    @BsonIgnore
    @Transient
    private List<BidResInfo> list;
}
