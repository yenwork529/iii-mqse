package org.iii.esd.mongo.vo.data.measure;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Meter12Data extends MeterData implements IMeasureData {

    /**
     * 負載 / 迴路編號
     */
    private Integer loadNo;

    // public Meter12Data(BigDecimal activePower, BigDecimal kWh, BigDecimal powerFactor, BigDecimal kVAR, BigDecimal kVA,
    //         BigDecimal voltageA, BigDecimal currentA, BigDecimal voltageB, BigDecimal currentB, BigDecimal voltageC,
    //         BigDecimal currentC, int loadNo) {
    //     super(activePower, kWh, powerFactor, kVAR, kVA, voltageA, currentA, voltageB, currentB, voltageC, currentC);
    //     this.loadNo = loadNo;
    // }

    @Override
    public MeasureData wrap() {
        return MeasureData.builder().
                activePower(activePower).
                                  kWh(kWh).
                                  powerFactor(powerFactor).
                                  kVAR(kVAR).
                                  kVA(kVA).
                                  voltageA(voltageA).
                                  currentA(currentA).
                                  voltageB(voltageB).
                                  currentB(currentB).
                                  voltageC(voltageC).
                                  currentC(currentC).
                                  loadNo(loadNo).
                                  build();
    }

}