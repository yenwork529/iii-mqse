package org.iii.esd.mongo.document.integrate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.iii.esd.mongo.document.UuidDocument;
import org.iii.esd.mongo.vo.data.measure.MeterData;
import org.springframework.data.annotation.LastModifiedDate;
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
@Document(collection = "GessResData")
public class GessResData extends UuidDocument {
    private String resId;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;

    private Long timeticks;

    @LastModifiedDate
    @JsonIgnore
    private Date updateTime;

    private BigDecimal m1VoltageA;

    private BigDecimal m1VoltageB;

    private BigDecimal m1VoltageC;

    private BigDecimal m1CurrentA;

    private BigDecimal m1CurrentB;

    private BigDecimal m1CurrentC;

    private BigDecimal m1Frequency;

    private BigDecimal m1PF;

    private BigDecimal m1kW;

    private BigDecimal m1kVar;

    private BigDecimal m1EnergyIMP;

    private BigDecimal m1EnergyEXP;

    private BigDecimal e1SOC;

    private BigDecimal e1Status;

    private BigDecimal e1Sbspm;

    public GessResData(String resId, Long ticks) {
        this.resId = resId;
        this.timeticks = (ticks / 1000L) * 1000L;
        this.timestamp = new Date(this.timeticks);
        super.setId(resId + "_" + this.timeticks.toString());
    }

    public static GessResData from(String resId, MeterData md) {
        GessResData ge = GessResData.builder() //
                .resId(resId)
                .timestamp(md.getTimestamp())
                .timeticks(md.getTimestamp().getTime())
                .e1SOC(md.getSoc())
                .e1Sbspm(md.getSbspm())
                .e1Status(md.getStatus())
                .m1CurrentA(md.getCurrentA())
                .m1VoltageA(md.getVoltageA())
                .m1CurrentB(md.getCurrentB())
                .m1VoltageB(md.getVoltageB())
                .m1CurrentC(md.getCurrentC())
                .m1VoltageC(md.getVoltageC())
                .m1kVar(md.getKVAR())
                .m1kW(md.getActivePower())
                .m1EnergyEXP(md.getEnergyExp())
                .m1EnergyIMP(md.getEnergyImp())
                .m1Frequency(md.getFrequency())
                .m1PF(md.getPowerFactor())
                .build();
        ge.setId(resId + "_" + ge.timeticks.toString());
        return ge;
    }
}
