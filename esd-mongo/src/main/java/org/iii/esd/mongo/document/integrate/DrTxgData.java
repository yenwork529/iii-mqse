package org.iii.esd.mongo.document.integrate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.iii.esd.mongo.document.UuidDocument;
import org.springframework.data.annotation.LastModifiedDate;
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
@Document(collection = "DrTxgData")
@CompoundIndexes({
// @CompoundIndex(def = "{'txgId':1, 'timestamp':1}",
// name = "pk_automaticFrequencyControlLog",
// unique = true)
// TODO 須設定
})
public class DrTxgData extends UuidDocument {
    // @DBRef
    // @Field("txgId")
    // private TxgProfile txgProfile;

    private String txgId;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;
    private Long timeticks;

    @LastModifiedDate
    @JsonIgnore
    private Date updateTime;

    private BigDecimal g1M1kW;
    private BigDecimal g1M1EnergyImp;
    private BigDecimal g1M1EnergyExp;
    private BigDecimal g1M1EnergyNet;
    private BigDecimal g1Performance;

    public DrTxgData add(DrResData resdata){
        this.g1M1kW = Add(this.g1M1kW, resdata.getM1kW());
        this.g1M1EnergyImp = Add(this.g1M1EnergyImp, resdata.getM1EnergyIMP());
        this.g1M1EnergyExp = Add(this.g1M1EnergyExp, resdata.getM1EnergyEXP());
        this.g1M1EnergyNet = Add(this.g1M1EnergyNet, resdata.getM1EnergyNET());
        // this.g1Performance = Add(this.g1Performance, resdata.getDr1Performance());
        return this;
    }

    public static BigDecimal Add(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return BigDecimal.ZERO;
        }
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.add(b);
    }

    // public DrTxgData plus(DrResData r) {
    //     g1M1EnergyNet = Add(g1M1EnergyNet, r.getM1EnergyNET());
    //     g1M1kW = Add(g1M1kW, r.getM1kW());
    //     return this;
    // }

    public DrTxgData(String txgId, Long ticks) {
        this.txgId = txgId;
        this.timestamp = new Date(ticks);
        this.timeticks = ticks;
        super.setId(txgId + "_" + ticks);
    }
}
