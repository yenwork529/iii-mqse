package org.iii.esd.mongo.document.integrate;

import java.util.List;
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
@Document(collection = "GessTxgData")
@CompoundIndexes({
// @CompoundIndex(def = "{'txgId':1, 'timestamp':1}",
// name = "pk_automaticFrequencyControlLog",
// unique = true)
// TODO 須設定
})

public class GessTxgData extends UuidDocument {

    private String txgId;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;

    private Long timeticks;

    @LastModifiedDate
    @JsonIgnore
    private Date updateTime;

    private BigDecimal r1VoltageA;

    private BigDecimal r1VoltageB;

    private BigDecimal r1VoltageC;

    private BigDecimal r1CurrentA;

    private BigDecimal r1CurrentB;

    private BigDecimal r1CurrentC;

    private BigDecimal r1Frequency;

    private BigDecimal r1FPF;

    private BigDecimal g1M1kW;

    private BigDecimal g1M1kWVar;

    private BigDecimal g1M1EnergyIMP;

    private BigDecimal g1M1EnergyEXP;

    private BigDecimal g1Sbspm;

    private BigDecimal g1SOC;

    private BigDecimal g1Status;

    public GessTxgData(String txgId, Long ticks) {
        this.txgId = txgId;
        this.timestamp = new Date(ticks);
        this.timeticks = ticks;
        super.setId(txgId + "_" + ticks);
    }

    public static GessTxgData from(String txgId, GessResData gr) {
        GessTxgData gd = GessTxgData.builder() //
                .txgId(txgId)
                .timestamp(gr.getTimestamp())
                .timeticks(gr.getTimeticks())
                .r1CurrentA(gr.getM1CurrentA())
                .r1CurrentB(gr.getM1CurrentB())
                .r1CurrentC(gr.getM1CurrentC())
                .r1FPF(gr.getM1PF())
                .r1Frequency(gr.getM1Frequency())
                .r1VoltageA(gr.getM1VoltageA())
                .r1VoltageB(gr.getM1VoltageB())
                .r1VoltageC(gr.getM1VoltageC())
                .g1M1EnergyEXP(gr.getM1EnergyEXP())
                .g1M1EnergyIMP(gr.getM1EnergyIMP())
                .g1M1kW(gr.getM1kW())
                .g1M1kWVar(gr.getM1kVar())
                .g1SOC(gr.getE1SOC())
                .g1Sbspm(gr.getE1Sbspm())
                .g1Status(gr.getE1Status())
                .build();
        gd.setId(txgId + "_" + gr.getTimeticks());
        return gd;
    }

    public GessTxgData add(GessResData gr) {
        return this;
    }

    public GessTxgData add(List<GessResData> grlst) {
        for (GessResData gr : grlst) {
            this.add(gr);
        }
        return this;
    }

}
