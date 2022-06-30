package org.iii.esd.server.wrap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.iii.esd.api.vo.integrate.DRegData;
import org.iii.esd.mongo.document.integrate.GessResData;
import org.iii.esd.mongo.document.integrate.GessTxgData;

import static org.iii.esd.api.vo.integrate.DRegData.DEFAULT_BASE_FREQ;
import static org.iii.esd.api.vo.integrate.DRegData.VOLTAGE_PU_SCALE;
import static org.iii.esd.utils.OptionalUtils.or;

public class GessDataWrapper {

    public static DRegData unwrapResData(Optional<GessResData> entity, long defautTimeticks) {
        return entity.isPresent() ?
                unwrapResData(entity.get()) : DRegData.builderOfTimeticks(defautTimeticks)
                                                      .build();
    }

    public static DRegData unwrapResData(GessResData entity) {
        return DRegData.builderOfTimeticks(entity.getTimestamp().getTime())
                       .voltageA(entity.getM1VoltageA())
                       .voltageB(entity.getM1VoltageB())
                       .voltageC(entity.getM1VoltageC())
                       .currentA(entity.getM1CurrentA())
                       .currentB(entity.getM1CurrentB())
                       .currentC(entity.getM1CurrentC())
                       .energyEXP(entity.getM1EnergyEXP())
                       .energyIMP(entity.getM1EnergyIMP())
                       .powerFactor(entity.getM1PF())
                       .kW(entity.getM1kW())
                       .kVar(entity.getM1kVar())
                       .frequency(entity.getM1Frequency())
                       .sbspm(or(entity.getE1Sbspm(), BigDecimal.ZERO))
                       .soc(or(entity.getE1SOC(), BigDecimal.ZERO))
                       .status(entity.getE1Status())
                       .build();
    }

    public static DRegData unwrapResDataWithRatedVoltage(GessResData entity, BigDecimal ratedVoltage) {
        return DRegData.builderOfTimeticks(entity.getTimestamp().getTime())
                       .voltageA(calPuVoltage(entity.getM1VoltageA(), ratedVoltage))
                       .voltageB(calPuVoltage(entity.getM1VoltageB(), ratedVoltage))
                       .voltageC(calPuVoltage(entity.getM1VoltageC(), ratedVoltage))
                       .currentA(entity.getM1CurrentA())
                       .currentB(entity.getM1CurrentB())
                       .currentC(entity.getM1CurrentC())
                       .energyEXP(entity.getM1EnergyEXP())
                       .energyIMP(entity.getM1EnergyIMP())
                       .powerFactor(entity.getM1PF())
                       .kW(entity.getM1kW())
                       .kVar(entity.getM1kVar())
                       .frequency(entity.getM1Frequency())
                       .sbspm(or(entity.getE1Sbspm(), BigDecimal.ZERO))
                       .soc(or(entity.getE1SOC(), BigDecimal.ZERO))
                       .status(entity.getE1Status())
                       .build();
    }

    public static DRegData unwrapTxgData(GessTxgData entity) {
        return DRegData.builderOfTimeticks(entity.getTimestamp().getTime())
                       .voltageA(entity.getR1VoltageA())
                       .voltageB(entity.getR1VoltageB())
                       .voltageC(entity.getR1VoltageC())
                       .currentA(entity.getR1CurrentA())
                       .currentB(entity.getR1CurrentB())
                       .currentC(entity.getR1CurrentC())
                       .energyEXP(entity.getG1M1EnergyEXP())
                       .energyIMP(entity.getG1M1EnergyIMP())
                       .powerFactor(entity.getR1FPF())
                       .kW(entity.getG1M1kW())
                       .kVar(entity.getG1M1kWVar())
                       .frequency(entity.getR1Frequency())
                       .sbspm(or(entity.getG1Sbspm(), BigDecimal.ZERO))
                       .soc(or(entity.getG1SOC(), BigDecimal.ZERO))
                       .status(entity.getG1Status())
                       .baseFreq(DEFAULT_BASE_FREQ)
                       .build();
    }

    public static DRegData unwrapTxgDataWithRatedVoltage(GessTxgData entity, BigDecimal ratedVoltage) {
        return DRegData.builderOfTimeticks(entity.getTimestamp().getTime())
                       .voltageA(calPuVoltage(entity.getR1VoltageA(), ratedVoltage))
                       .voltageB(calPuVoltage(entity.getR1VoltageB(), ratedVoltage))
                       .voltageC(calPuVoltage(entity.getR1VoltageC(), ratedVoltage))
                       .currentA(entity.getR1CurrentA())
                       .currentB(entity.getR1CurrentB())
                       .currentC(entity.getR1CurrentC())
                       .energyEXP(entity.getG1M1EnergyEXP())
                       .energyIMP(entity.getG1M1EnergyIMP())
                       .powerFactor(entity.getR1FPF())
                       .kW(entity.getG1M1kW())
                       .kVar(entity.getG1M1kWVar())
                       .frequency(entity.getR1Frequency())
                       .sbspm(or(entity.getG1Sbspm(), BigDecimal.ZERO))
                       .soc(or(entity.getG1SOC(), BigDecimal.ZERO))
                       .status(entity.getG1Status())
                       .baseFreq(DEFAULT_BASE_FREQ)
                       .build();
    }

    private static BigDecimal calPuVoltage(BigDecimal voltage, BigDecimal ratedVoltage) {
        return or(voltage, BigDecimal.ZERO).divide(ratedVoltage, VOLTAGE_PU_SCALE, RoundingMode.HALF_UP);
    }
}
