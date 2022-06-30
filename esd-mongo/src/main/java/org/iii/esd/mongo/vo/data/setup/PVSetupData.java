package org.iii.esd.mongo.vo.data.setup;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PVSetupData implements ISetupData {

    /**
     * Current Transformer ratio
     */
    private BigDecimal ct;
    /**
     * Phase voltage Transformers ratio
     */
    private BigDecimal pt;
    /**
     * PV發電容量(kWp)
     */
    private Integer pvCapacity;
    /**
     * 單位採購成本($/kWp)
     */
    private Integer unitCost;
    /**
     * 維護費用(%)
     */
    private BigDecimal maintenanceCost;

    public SetupData wrap() {
        return SetupData.builder().
                ct(ct).
                                pt(pt).
                                pvCapacity(pvCapacity).
                                unitCost(unitCost).
                                maintenanceCost(maintenanceCost).
                                build();
    }

}