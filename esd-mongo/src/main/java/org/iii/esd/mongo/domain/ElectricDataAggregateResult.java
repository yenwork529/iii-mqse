package org.iii.esd.mongo.domain;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.iii.esd.mongo.document.UuidDocument;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ElectricDataAggregateResult extends UuidDocument {
    Date time;
    BigDecimal kW;
    public ElectricDataAggregateResult(Date date, Double value) {
        // TODO Auto-generated constructor stub
        this.time = date;
        this.kW = BigDecimal.valueOf(value);
    }
}
