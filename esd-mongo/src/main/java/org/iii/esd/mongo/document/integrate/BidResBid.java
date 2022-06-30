package org.iii.esd.mongo.document.integrate;

import lombok.*;

import org.iii.esd.mongo.document.UuidDocument;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "BidResBid")

public class BidResBid extends UuidDocument {

    private String resId;

    // timestamp 改為 Date 以相容原有程式, Gem, 2021/11/09
    private long timeticks;
    private Date timestamp;

    /**
     * 服務品質指標(%)
     */
    private BigDecimal serviceFactor;

    /**
     * 容量費用
     */
    private BigDecimal capacityRevenue;

    /**
     * 效能費 (NT)
     */
    private BigDecimal efficacyRevenue;

    /**
     * 電能費
     */
    private BigDecimal energyRevenue;

    /**
     * 每小時服務電量
     */
    private BigDecimal serviceEnergy;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date createTime;

    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date updateTime;


    public BidResBid(String resId, Date dt) {
        this.resId = resId;
        long hour = dt.getTime() / 1000 / 60 / 60;
        this.timeticks = hour * 1000 * 60 * 60;
        this.timestamp = new Date(this.timeticks);
        super.id = resId + String.format("_%d", hour);
    }
}
