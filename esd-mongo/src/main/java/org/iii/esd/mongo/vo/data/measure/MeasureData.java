package org.iii.esd.mongo.vo.data.measure;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.iii.esd.utils.MathUtils;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeasureData {

    /**
     * 功率(kW)
     */
    private BigDecimal activePower;

    /**
     * 累計度數(kWh)
     */
    private BigDecimal kWh;
    /**
     * 功率因數
     */
    private BigDecimal powerFactor;
    /**
     * 虛功率
     */
    private BigDecimal kVAR;
    /**
     * 即時視在功率
     */
    private BigDecimal kVA;
    /**
     * A 相（R 相）相電壓
     */
    private BigDecimal voltageA;
    /**
     * A 相（R 相）電流
     */
    private BigDecimal currentA;
    /**
     * B 相（S 相）相電壓
     */
    private BigDecimal voltageB;
    /**
     * B 相（S 相）電流
     */
    private BigDecimal currentB;
    /**
     * C 相（T 相）相電壓
     */
    private BigDecimal voltageC;
    /**
     * C 相（T 相）電流
     */
    private BigDecimal currentC;

    /**
     * 負載 / 迴路編號
     */
    private Integer loadNo;

    /**
     * 充放電狀態 0:standby 1:charge 2:discharge
     */
    private Integer status;
    /**
     * 電池殘留電量百分比(%)
     */
    private BigDecimal soc;
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

    private BigDecimal energyImp; // 2021-0917

    private BigDecimal energyExp; // 2021-0917

    private BigDecimal energyNet; // 2021-1101

    public void ratio(BigDecimal pt, BigDecimal ct) {
        if (pt != null && ct != null) {
            BigDecimal ctpt = MathUtils.multiply(ct, pt);

            activePower = MathUtils.multiply(activePower, ctpt);

            kWh = MathUtils.multiply(kWh, ctpt);
            kVAR = MathUtils.multiply(kVAR, ctpt);
            kVA = MathUtils.multiply(kVA, ctpt);

            voltageA = MathUtils.multiply(voltageA, pt);
            currentA = MathUtils.multiply(currentA, ct);

            voltageB = MathUtils.multiply(voltageB, pt);
            currentB = MathUtils.multiply(currentB, ct);

            voltageC = MathUtils.multiply(voltageC, pt);
            currentC = MathUtils.multiply(currentC, ct);
        }
    }

}