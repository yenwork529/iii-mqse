package org.iii.esd.mongo.vo.data.measure;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class MeterData implements IMeasureData {

    String devid;

    Date timestamp;

    /**
     * 功率(kW)
     */
    @JsonProperty("inst_kw")
    protected BigDecimal activePower;
    /**
     * 累計度數(kWh)
     */
    @JsonProperty("del_total_kwh")
    protected BigDecimal kWh;
    /**
     * 功率因數
     */
    @JsonProperty("inst_pf")
    protected BigDecimal powerFactor;
    /**
     * 虛功率
     */
    @JsonProperty("inst_kvar")
    protected BigDecimal kVAR;
    /**
     * 即時視在功率
     */
    @JsonProperty("inst_kva")
    protected BigDecimal kVA;
    /**
     * A 相（R 相）相電壓
     */
    @JsonProperty("phase_a_vol_v")
    protected BigDecimal voltageA;
    /**
     * A 相（R 相）電流
     */
    @JsonProperty("phase_a_cur_a")
    protected BigDecimal currentA;
    /**
     * B 相（S 相）相電壓
     */
    @JsonProperty("phase_b_vol_v")
    protected BigDecimal voltageB;
    /**
     * B 相（S 相）電流
     */
    @JsonProperty("phase_b_cur_a")
    protected BigDecimal currentB;
    /**
     * C 相（T 相）相電壓
     */
    @JsonProperty("phase_c_vol_v")
    protected BigDecimal voltageC;
    /**
     * C 相（T 相）電流
     */
    @JsonProperty("phase_c_cur_a")
    protected BigDecimal currentC;

    @JsonProperty("energy_imp") // 2021-1101
    private BigDecimal energyImp; // 2021-0917

    @JsonProperty("energy_exp") // 2021-1101
    private BigDecimal energyExp; // 2021-0917

    @JsonProperty("energy_net") // 2021-1101
    private BigDecimal energyNet; // 2021-1101

    private BigDecimal frequency; // Hz, scale = 2
    BigDecimal soc; // in percentage %, scale = 0
    BigDecimal status; // 1 or zero
    BigDecimal sbspm; // int percentage %, scale = 0(?)

    @Override
    public MeasureData wrap() {
        return MeasureData.builder().activePower(activePower).kWh(kWh).powerFactor(powerFactor).kVAR(kVAR).kVA(kVA)
                .voltageA(voltageA).currentA(currentA).voltageB(voltageB).currentB(currentB).voltageC(voltageC)
                .currentC(currentC).energyExp(energyExp).energyImp(energyImp).energyNet(energyNet).build();
    }

    public static MeterData of(String devid, Date timestamp, BigDecimal totalKw, BigDecimal energyNet) {
        return MeterData.builder()
                .devid(devid)
                .timestamp(timestamp)
                .activePower(totalKw) // "inst_kw"
                .kWh(energyNet) // "del_total_kwh"
                .energyNet(energyNet) // energy_net
                .build();
    }

    public boolean isActive(){
        return status.intValue() == 1;
    }

    public int x100Frequency(){
        return frequency.divide(BigDecimal.valueOf(0.01), 2, RoundingMode.HALF_UP).intValue();
    }

    public MeterData resetSbspm(){
        sbspm = BigDecimal.ZERO;
        return this;
    }
}