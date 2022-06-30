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
public class ControllerSetupData implements ISetupData {

    /**
     * Current Transformer ratio
     */
    private BigDecimal ct;
    /**
     * Phase voltage Transformers ratio
     */
    private BigDecimal pt;
    /**
     * 滿載容量(kW)
     */
    private BigDecimal fullCapacity;
    /**
     * 可卸容量(kW)
     */
    private BigDecimal unloadCapacity;
    /**
     * 卸載時間(秒)
     */
    @Builder.Default
    private Integer unloadTime = 0;
    /**
     * 覆歸時間(秒)
     */
    @Builder.Default
    private Integer returnTime = 0;
    /**
     * 採購成本($)
     */
    private BigDecimal cost;
    /**
     * 維護費用(%)
     */
    private BigDecimal maintenanceCost;

    public SetupData wrap() {
        return SetupData.builder().
                ct(ct).
                                pt(pt).
                                fullCapacity(fullCapacity).
                                unloadCapacity(unloadCapacity).
                                unloadTime(unloadTime).
                                returnTime(returnTime).
                                cost(cost).
                                maintenanceCost(maintenanceCost).
                                build();
    }

}