package org.iii.esd.mongo.vo.data.measure;

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
public class BatteryData implements IMeasureData {

    /**
     * 充放電狀態 0:standby 1:charge 2:discharge
     */
    private Integer status;
    /**
     * 電池殘留電量百分比(%)
     */
    private BigDecimal soc;
    /**
     * 功率(kW)
     */
    private BigDecimal activePower;
    /**
     * 充電累計度數(kWh)
     */
    private BigDecimal chargeKWh;
    /**
     * 放電累計度數(kWh)
     */
    private BigDecimal dischargeKWh;
    /**
     * 電池溫度
     */
    private BigDecimal temperature;
    /**
     * DC 總電壓
     */
    private BigDecimal voltage;
    /**
     * DC 總電流
     */
    private BigDecimal current;

    @Override
    public MeasureData wrap() {
        return MeasureData.builder().
                status(status).
                                  soc(soc).
                                  activePower(activePower).
                                  chargeKWh(chargeKWh).
                                  dischargeKWh(dischargeKWh).
                                  temperature(temperature).
                                  voltage(voltage).
                                  current(current).
                                  build();
    }

}