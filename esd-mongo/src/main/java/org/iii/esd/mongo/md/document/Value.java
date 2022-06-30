package org.iii.esd.mongo.md.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Value {

    // 即時功率(kW)
    private Double inst_kw;
    // 累計用電量(kWh)
    private Double del_total_kwh;

    private Double Total_Charge_KWH;

    private Double Total_DisCharge_KWH;

    private Long[] disconnect;

    public Value(Double inst_kw, Double del_total_kwh) {
        this.inst_kw = inst_kw;
        this.del_total_kwh = del_total_kwh;
    }

}