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
public class BatterySetupData implements ISetupData {

    /**
     * 額定電壓
     */
    private BigDecimal ratedVoltage;
    /**
     * 蓄電容量(kWh)
     */
    private BigDecimal capacity;
    /**
     * 最大充電功率(kW)
     */
    private BigDecimal chargeKw;
    /**
     * 最大放電功率(kW)
     */
    private BigDecimal dischargeKw;
    /**
     * 充電效率(%)
     */
    private BigDecimal chargeEfficiency;
    /**
     * 放電效率(%)
     */
    private BigDecimal dischargeEfficiency;
    /**
     * 放電深度(%)
     */
    private BigDecimal dod;
    /**
     * SOC充電上限(%)
     */
    @Builder.Default
    private Integer socMax = 100;
    /**
     * SOC底線(%)
     */
    @Builder.Default
    private Integer socMin = 0;
    /**
     * 自放電功率(kW)
     */
    private BigDecimal selfDischargeKw;
    /**
     * 循環壽命(次數)
     */
    private Integer lifecycle;
    /**
     * 施工費用($)
     */
    @Builder.Default
    private Integer constructionCost = 0;
    /**
     * 單位容量成本($/kWh)
     */
    private Integer capacityCost;
    /**
     * 單位功率成本($/kW)
     */
    private Integer kWcost;
    /**
     * 維護費用(%)
     */
    private BigDecimal maintenanceCost;

    public SetupData wrap() {
        return SetupData.builder().
                capacity(capacity).
                                chargeKw(chargeKw).
                                dischargeKw(dischargeKw).
                                chargeEfficiency(chargeEfficiency).
                                dischargeEfficiency(dischargeEfficiency).
                                dod(dod).
                                socMax(socMax).
                                socMin(socMin).
                                selfDischargeKw(selfDischargeKw).
                                lifecycle(lifecycle).
                                constructionCost(constructionCost).
                                capacityCost(capacityCost).
                                kWcost(kWcost).
                                maintenanceCost(maintenanceCost).
                                build();
    }

}