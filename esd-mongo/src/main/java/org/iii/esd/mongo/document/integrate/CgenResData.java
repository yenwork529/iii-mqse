package org.iii.esd.mongo.document.integrate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.UuidDocument;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection="CgenResData")
public class CgenResData extends UuidDocument {

    private String resId;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;

    @LastModifiedDate
    @JsonIgnore
    private Date updateTime;

    // private int dataType;//TODO 須設定

    // private Boolean needFix;

    private BigDecimal m1kW;

    private BigDecimal m1EnergyIMP;

    private BigDecimal m1EnergyEXP;

    private BigDecimal m1EnergyNET; // from M1's totalkWh 2021-1014

    private BigDecimal m2kW;

    private BigDecimal gen1Status; // ??

    // 2021-1014
    // 1. 檢查 SpinReserveDataDetail 是否存在，若否，則表示未被調度 -> Performance = 100%
    // 2. 檢查 SpinReserveData，看 noticeTime, startTime(?)
    private BigDecimal gen1Performance; // 與該場域降載目標 ClipKW 相除取百分比

    public CgenResData(String resId){
        this.resId = resId;
    }

    public static CgenResData from(String resId, ElectricData ed) {
        CgenResData y = new CgenResData(resId);
        y.setGen1Performance(BigDecimal.valueOf(100L));
        y.setGen1Status(BigDecimal.ZERO);
        y.setM1EnergyEXP(ed.getEnergyExp());
        y.setM1EnergyIMP(ed.getEnergyImp());
        y.setM1EnergyNET(ed.getTotalkWh());
        y.setM1kW(ed.getM1kW());
        y.setM2kW(ed.getM2kW());
        return y;
    }
}
