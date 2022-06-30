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
@Document(collection = "RegulationHistory")

public class RegulationHistory extends UuidDocument {

    private String txgId; // 2022-0114 changed from resId to txgId

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;
    private Long timeticks;

    @LastModifiedDate
    @JsonIgnore
    private Date updateTime;

    Long targetFrequency; // Hz x100
    Long targetFreqTime; // in epoch seconds
    Long dischargeCapacity; // KW, '+' for discharge, '-' for charge
    Long dischargeTime; // in epoch seconds

    // public RegulationHistory(String resId, Long ticks) {
    //     this.resId = resId;
    //     this.timeticks = (ticks / 1000L) * 1000L;
    //     this.timestamp = new Date(this.timeticks);
    //     super.setId(resId + "_" + this.timeticks.toString());
    // }

    public RegulationHistory(String txgId, long freq, long freqtime) {
        this.txgId = txgId;
        long ticks = System.currentTimeMillis();
        this.timeticks = (ticks / 1000L) * 1000L;
        this.timestamp = new Date(this.timeticks);
        this.targetFrequency = freq;
        this.targetFreqTime = freqtime;
        // Todo: not sure the unique 
        // super.setId(resId + "_" + this.targetFreqTime.toString());
    }

}
