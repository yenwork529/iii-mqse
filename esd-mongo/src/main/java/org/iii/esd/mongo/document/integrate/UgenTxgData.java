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
@Document(collection = "UgenTxgData")
@CompoundIndexes({
//        @CompoundIndex(def = "{'txgId':1, 'timestamp':1}",
//                name = "pk_automaticFrequencyControlLog",
//                unique = true)
        //TODO 須設定
})
public class UgenTxgData extends UuidDocument {
//    @DBRef
//    @Field("txgId")
//    private TxgProfile txgProfile;

    private String txgId;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;

    @LastModifiedDate
    @JsonIgnore
    private Date updateTime;

    private int dataType;

    private boolean needFix;

    private BigDecimal r1VoltageA;

    private BigDecimal r1VoltageB;

    private BigDecimal r1VoltageC;

    private BigDecimal r1CurrentA;

    private BigDecimal r1CurrentB;

    private BigDecimal r1CurrentC;

    private BigDecimal r1Frequency;

    private BigDecimal g1M1kW;

    private BigDecimal g1M1EnergyIMP;

    private BigDecimal g1M1EnergyEXP;

    private BigDecimal g1M2kW;

    private BigDecimal g1Performance;
}
