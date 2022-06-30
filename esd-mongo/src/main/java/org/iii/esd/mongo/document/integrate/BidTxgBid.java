package org.iii.esd.mongo.document.integrate;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.iii.esd.mongo.document.UuidDocument;
import org.iii.esd.utils.DatetimeUtils;

import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "BidTxgBid")

public class BidTxgBid extends UuidDocument  {

    private long timeticks;

    private Date timestamp;

    private String txgId;

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
     * 每小時服務電能量
     */
    private BigDecimal serviceEnergy;

    // for abandon calculation, treat as ONE (100%) if not present
    private BigDecimal dutyCycle; 

    public BidTxgBid(String txgId, Date dt){
        this.txgId = txgId;
        long hour = dt.getTime() / 1000 / 60 / 60;
        this.timeticks = hour * 1000 * 60 * 60;
        this.timestamp = new Date(this.timeticks);
        super.id = txgId + String.format("_%d", hour);
    }
    @Transient
    @BsonIgnore
    private List<BidResBid> list;
}
