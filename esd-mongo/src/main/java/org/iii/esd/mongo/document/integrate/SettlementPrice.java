package org.iii.esd.mongo.document.integrate;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import org.iii.esd.exception.ApplicationException;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.UuidDocument;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "SettlementPrice")
public class SettlementPrice extends UuidDocument {


    /**
     * 交易日期/小時(per hour)
     */
    private Date timestamp;
    private Long timeticks;

    /**
     * 調頻備轉結清價格
     */
    private BigDecimal afcSettlementPrice;

    /**
     * 即時備轉結清價格
     */
    private BigDecimal srSettlementPrice;

    /**
     * 補充備轉結清價格
     */
    private BigDecimal supSettlementPrice;

    /**
     * 電能邊際價格
     */
    private BigDecimal marginalElectricPrice;

    public SettlementPrice(Date dt) {
        this.dt = dt;
        initial();

//        long hour = dt.getTime() / 1000 / 60 / 60;
//        this.timeticks = hour * 1000 * 60 * 60;
//        this.timestamp = new Date(this.timeticks);
//        super.id = String.format("%d", hour);
    }

    public SettlementPrice fix() {
        if (marginalElectricPrice == null) {
            marginalElectricPrice = BigDecimal.ZERO;
        }
        if (srSettlementPrice == null) {
            srSettlementPrice = BigDecimal.ZERO;
        }
        if (supSettlementPrice == null) {
            supSettlementPrice = BigDecimal.ZERO;
        }
        if (afcSettlementPrice == null) {
            afcSettlementPrice = BigDecimal.ZERO;
        }
        return this;
    }

    @BsonIgnore
    @Transient
    private Date dt;

    public SettlementPrice initial() {
        if (dt == null) {
            throw new ApplicationException(Error.invalidParameter, "dt");
        }

        long hour = dt.getTime() / 1000 / 60 / 60;
        this.timeticks = hour * 1000 * 60 * 60;
        this.timestamp = new Date(this.timeticks);
        super.id = String.format("%d", hour);

        return this;
    }
}
