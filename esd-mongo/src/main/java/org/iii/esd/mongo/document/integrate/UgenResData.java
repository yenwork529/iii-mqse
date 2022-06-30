package org.iii.esd.mongo.document.integrate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.iii.esd.mongo.document.UuidDocument;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.Date;

public class UgenResData extends UuidDocument {
//    @DBRef
//    @Field("resId")
//    private TxgFieldProfile txgFieldProfile;

    private String resId;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;

    @LastModifiedDate
    @JsonIgnore
    private Date updateTime;


    private BigDecimal m1VoltageA;

    private BigDecimal m1VoltageB;

    private BigDecimal m1VoltageC;

    private BigDecimal m1CurrentA;

    private BigDecimal m1CurrentB;

    private BigDecimal m1CurrentC;

    private BigDecimal m1kW;

    private BigDecimal m1Frequency;

    private BigDecimal m1EnergyIMP;

    private BigDecimal m1EnergyEXP;

    private BigDecimal m2kW;

    private BigDecimal dr1Status;

    private BigDecimal dr1Performance;
}
