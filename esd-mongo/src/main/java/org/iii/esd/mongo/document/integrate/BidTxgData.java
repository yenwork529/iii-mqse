package org.iii.esd.mongo.document.integrate;

import java.math.BigDecimal;
import java.util.*;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonFormat;
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

import org.iii.esd.enums.NoticeType;
import org.iii.esd.mongo.document.UuidDocument;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "BidTxgData")

public class BidTxgData extends UuidDocument  {

    private String txgId;

    Map<String,BigDecimal> ratioMap;

    private Long timeticks;
    private Date timestamp;
    /**
     * 通知類型
     */
    @Enumerated(EnumType.STRING)
    private NoticeType noticeType;

    /**
     * 通知時間(t1)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date noticeTime;

    /**
     * 開始時間(t2)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date startTime;

    /**
     * 結束時間(t4)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date endTime;

    /**
     * 資料更新時間
     */
    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date updateTime;

    /**
     * 卸載量(得標量)
     */
    private Integer clipKW;

    /**
     * 基準線(通知前5分鐘平均需量)
     */
    private BigDecimal baseline;

    /**
     * 執行每分鐘平均降載容量
     */
    private BigDecimal clippedKW;

    /**
     * 每分鐘平均執行率(%)
     */
    private BigDecimal revenueFactor;

    /**
     * 服務提供之電能量(kWh)
     */
    private BigDecimal serviceEnergy;

    /**
     * 執行率期間提供之電能量(kWh)
     */

    private BigDecimal performanceEnergy;

    public BidTxgData(String txgId, Date dt){
        this.txgId = txgId;
        long hour = dt.getTime(); // / 1000 / 60 / 60;
        this.timeticks = hour; // * 1000 * 60 * 60;
        this.timestamp = dt;
        super.id = txgId + String.format("_%d", hour);
    }
    @BsonIgnore
    @Transient
    private List<BidResData> list;
}
