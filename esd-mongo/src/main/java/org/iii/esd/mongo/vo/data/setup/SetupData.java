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
@NoArgsConstructor
@AllArgsConstructor
public class SetupData {

    // 電池(M3)
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
    private Integer socMax;
    /**
     * SOC底線(%)
     */
    private Integer socMin;
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
    private Integer constructionCost;
    /**
     * 單位容量成本($/kWh)
     */
    private Integer capacityCost;
    /**
     * 單位功率成本($/kW)
     */
    private Integer kWcost;

    // 可控負載(M6)
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
    private Integer unloadTime;
    /**
     * 覆歸時間(秒)
     */
    private Integer returnTime;
    /**
     * 採購成本($)
     */
    private BigDecimal cost;

    // 燃料電池(M8)、發電機(M10)
    /**
     * 發電功率(kW)
     */
    private BigDecimal power;

    // PV(M2)、躉售PV(M21)
    /**
     * 太陽能發電容量(kWp)
     */
    private Integer pvCapacity;

    // 共通欄位
    /**
     * 單位採購成本($/kW)
     */
    private Integer unitCost;
    /**
     * 維護費用(%)
     */
    private BigDecimal maintenanceCost;

    // 基本電錶欄位
    /**
     * Current Transformer ratio
     */
    private BigDecimal ct;
    /**
     * Phase voltage Transformers ratio
     */
    private BigDecimal pt;
}