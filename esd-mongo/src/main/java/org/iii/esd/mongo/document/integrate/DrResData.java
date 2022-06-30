package org.iii.esd.mongo.document.integrate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.Hash32;
import org.iii.esd.mongo.document.UuidDocument;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "DrResData")
@CompoundIndexes({
// @CompoundIndex(def = "{'txgId':1, 'timestamp':1}",
// name = "pk_automaticFrequencyControlLog",
// unique = true)
// TODO 須設定
})

public class DrResData extends UuidDocument {

    private String resId;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;
    private Long timeticks;

    @LastModifiedDate
    @JsonIgnore
    private Date updateTime;

    private BigDecimal m1kW;

    private BigDecimal m1EnergyIMP;

    private BigDecimal m1EnergyEXP;

    private BigDecimal m1EnergyNET;

    private BigDecimal dr1Status;

    private BigDecimal dr1Performance;

    public DrResData(String resId, Long ticks) {
        this.resId = resId;
        this.timeticks = (ticks / 1000L) * 1000L;
        this.timestamp = new Date(this.timeticks);
        super.setId(resId + "_" + this.timeticks.toString());
    }

    public static DrResData from(String resId, ElectricData ed) {
        DrResData y = new DrResData(resId, ed.getTime().getTime());
        y.setDr1Performance(BigDecimal.valueOf(100L));
        y.setDr1Status(BigDecimal.ZERO);
        y.setM1EnergyEXP(ed.getEnergyExp());
        y.setM1EnergyIMP(ed.getEnergyImp());
        y.setM1EnergyNET(ed.getTotalkWh());
        y.setM1kW(ed.getActivePower());
        return y;
    }

    public static DrResData from(TxgElectricData ed) {
        DrResData y = new DrResData(ed.getResId(), ed.getTimestamp().getTime());
        y.setDr1Performance(BigDecimal.valueOf(100L));
        y.setDr1Status(BigDecimal.ZERO);
        y.setM1EnergyEXP(ed.getM1EnergyNET());
        y.setM1EnergyIMP(ed.getM1EnergyIMP());
        y.setM1EnergyNET(ed.getM1EnergyNET());
        y.setM1kW(ed.getM1kW());
        return y;
    }
}
