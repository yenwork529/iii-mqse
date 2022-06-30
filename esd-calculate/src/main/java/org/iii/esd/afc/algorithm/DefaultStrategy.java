package org.iii.esd.afc.algorithm;

import lombok.extern.log4j.Log4j2;

import org.iii.esd.afc.def.FPMappingEnum;
import org.iii.esd.afc.def.PowerRatioEnum;
import org.iii.esd.afc.def.ZoneEnum;

import static org.iii.esd.afc.def.FrequencyEnum.A;
import static org.iii.esd.afc.def.FrequencyEnum.B;
import static org.iii.esd.afc.def.FrequencyEnum.C;
import static org.iii.esd.afc.def.FrequencyEnum.D;
import static org.iii.esd.afc.def.FrequencyEnum.E;
import static org.iii.esd.afc.def.FrequencyEnum.F;
import static org.iii.esd.afc.def.PowerRatioEnum.o;
import static org.iii.esd.afc.def.PowerRatioEnum.t;
import static org.iii.esd.afc.def.PowerRatioEnum.u;
import static org.iii.esd.afc.def.PowerRatioEnum.x;
import static org.iii.esd.afc.def.PowerRatioEnum.y;
import static org.iii.esd.afc.utils.Calculator.getAverage;
import static org.iii.esd.afc.utils.Calculator.getFractionDigits;
import static org.iii.esd.afc.utils.Calculator.getHalfAmount;

@Log4j2
public class DefaultStrategy implements Strategy {

    public static Double SOC_BASELINE = 50.0;  //State-Of-Charge Baseline

    private Double frequency;

    private Double soc;

    public DefaultStrategy(Double frequency, Double soc) {
        this.frequency = frequency;
        this.soc = soc;
    }

    private Double process(FPMappingEnum mapping) {
        switch (mapping) {
            case REF_POINT_AB_tu:
                log.debug(mapping.getZone() + ", " + mapping.getRange());
                // 二維內插法(59.50<=(input frequency)<59.75 ==> 100<=(??)<48)
                double[] x_AB_tu = {A.getFrequency(), getAverage(A, B), B.getFrequency()};
                double[] y_AB_tu = {t.getPowerRatio(), getAverage(t, u), u.getPowerRatio()};
                double p1 = Interpolation.calculate(x_AB_tu, y_AB_tu, frequency);
                return getFractionDigits(p1, 2);
            case REF_POINT_BC_uo:
                log.debug(mapping.getZone() + ", " + mapping.getRange());
                // 二維內插法(59.75<=(input frequency)<59.98 ==> 48<=(??)<0)
                double[] x_BC_uo = {B.getFrequency(), getAverage(B, C), C.getFrequency()};
                double[] y_BC_uo = {u.getPowerRatio(), getAverage(u, o), o.getPowerRatio()};
                double p2 = Interpolation.calculate(x_BC_uo, y_BC_uo, frequency);
                return getFractionDigits(p2, 2);
            case REF_POINT_CD_vw:
                log.debug(mapping.getZone());
                // 59.98<=(input frequency)<=60.02  ==>  4.5/-4.5
                if (soc < SOC_BASELINE) { return getHalfAmount(mapping.getRange().getLowerLimit().getPowerRatio()); } else if (soc >
                        SOC_BASELINE) {
                    return getHalfAmount(mapping.getRange().getUpperLimit().getPowerRatio());
                } else // soc==SOC_BASELINE
                { return PowerRatioEnum.o.getPowerRatio(); }
            case REF_POINT_DE_ox:
                log.debug(mapping.getZone() + ", " + mapping.getRange());
                // 二維內插法(60.02<(input frequency)<=60.25 ==> 0<(??)<=-48)
                double[] x_DE_ox = {D.getFrequency(), getAverage(D, E), E.getFrequency()};
                double[] y_DE_ox = {o.getPowerRatio(), getAverage(o, x), x.getPowerRatio()};
                double p4 = Interpolation.calculate(x_DE_ox, y_DE_ox, frequency);
                return getFractionDigits(p4, 2);
            case REF_POINT_EF_xy:
                log.debug(mapping.getZone() + ", " + mapping.getRange());
                // 二維內插法(60.25<(input frequency)<=60.50 ==> -48<(??)=<-100)
                double[] x_EF_xy = {E.getFrequency(), getAverage(E, F), F.getFrequency()};
                double[] y_EF_xy = {x.getPowerRatio(), getAverage(x, y), y.getPowerRatio()};
                double p5 = Interpolation.calculate(x_EF_xy, y_EF_xy, frequency);
                return getFractionDigits(p5, 2);
            default:
                log.error("Undefined algorithm [mapping=" + mapping + "]");
                break;
        }
        return null;
    }

    @Override
    public Double execute() {
        if (frequency == null || soc == null) {
            String errMsg = "Frequency or soc is null [frequency=" + frequency + ", soc=" + soc + "]";
            log.error(errMsg);
            throw new IllegalArgumentException(errMsg);
        }

        ZoneEnum zone = ZoneEnum.of(frequency);
        if (zone == null || ZoneEnum.UNDEFINED_ZONE == zone) {
            String errMsg = "Undefined zone [frequency=" + frequency + "]";
            log.error(errMsg);
            throw new UnsupportedOperationException(errMsg);
        }

        FPMappingEnum mapping = FPMappingEnum.of(zone);
        if (mapping == null || FPMappingEnum.UNDEFINED_MAPPING == mapping) {
            String errMsg = "Undefined mapping [zone=" + zone + "]";
            log.error(errMsg);
            throw new UnsupportedOperationException();
        }
        return process(mapping);
    }
}
