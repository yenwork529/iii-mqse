package org.iii.esd.afc.performance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import org.iii.esd.afc.algorithm.DefaultStrategy;
import org.iii.esd.afc.algorithm.Interpolation;
import org.iii.esd.afc.def.ZoneEnum;
import org.iii.esd.afc.service.ConvertService;
import org.iii.esd.afc.utils.AreaUtils;
import org.iii.esd.afc.utils.Point;

import static org.iii.esd.afc.def.FrequencyEnum.B;
import static org.iii.esd.afc.def.FrequencyEnum.C;
import static org.iii.esd.afc.def.FrequencyEnum.D;
import static org.iii.esd.afc.def.FrequencyEnum.E;
import static org.iii.esd.afc.def.PointEnum.Bu;
import static org.iii.esd.afc.def.PointEnum.Cv;
import static org.iii.esd.afc.def.PointEnum.Cw;
import static org.iii.esd.afc.def.PointEnum.Dv;
import static org.iii.esd.afc.def.PointEnum.Dw;
import static org.iii.esd.afc.def.PointEnum.Ex;
import static org.iii.esd.afc.def.PowerRatioEnum.u;
import static org.iii.esd.afc.def.PowerRatioEnum.v;
import static org.iii.esd.afc.def.PowerRatioEnum.w;
import static org.iii.esd.afc.def.PowerRatioEnum.x;
import static org.iii.esd.afc.def.ZoneEnum.DEAD_BAND;
import static org.iii.esd.afc.def.ZoneEnum.UNDEFINED_ZONE;
import static org.iii.esd.afc.utils.Calculator.getAverage;
import static org.iii.esd.afc.utils.Calculator.getFractionDigits;
import static org.iii.esd.afc.utils.Calculator.isInverseRatioRelationship;

@Log4j2
@Component
@Data
@Scope("prototype")
public class PMcalSbspm implements PMcal {

    public Double[] frequencies;

    public Double[] actualPowerRatios;

    @Autowired
    private ConvertService service;

    private BigDecimal HUNDRED = new BigDecimal(100);

    // three point(x,y) in Triangle(Zone FIRST_BAND_DISCHARGE)
    private Point P_Bu = new Point(Bu.getFrequency(), Bu.getPowerRatio());
    private Point P_Cv = new Point(Cv.getFrequency(), Cv.getPowerRatio());
    private Point P_Cw = new Point(Cw.getFrequency(), Cw.getPowerRatio());

    // three point(x,y) in Triangle(Zone FIRST_BAND_CHARGE)
    private Point P_Dv = new Point(Dv.getFrequency(), Dv.getPowerRatio());
    private Point P_Dw = new Point(Dw.getFrequency(), Dw.getPowerRatio());
    private Point P_Ex = new Point(Ex.getFrequency(), Ex.getPowerRatio());

    public PMcalSbspm() {}

    public PMcalSbspm(Double[] frequencies, Double[] actualPowerRatios) {
        this.frequencies = frequencies;
        this.actualPowerRatios = actualPowerRatios;
    }

    @Override
    public BigDecimal calculate() {
        if (frequencies.length != 2 || actualPowerRatios.length != 2) { return null; }

        Double powerRatio = service.run(frequencies[1], DefaultStrategy.SOC_BASELINE);
        ZoneEnum whichZone = ZoneEnum.of(frequencies[1]);
        if (powerRatio == null || whichZone == UNDEFINED_ZONE) { return null; }

        // check if actual power ration is inside envelope
        BigDecimal sbspm = null;
        switch (whichZone) {
            case FULL_BAND_OUTPUT:
                if (actualPowerRatios[1] == powerRatio) { sbspm = HUNDRED; }
                break;
            case FIRST_BAND_DISCHARGE:
                Point P_FBD = new Point(new BigDecimal(frequencies[1]), new BigDecimal(actualPowerRatios[1]));
                if (AreaUtils.isInTriangle(P_Bu, P_Cv, P_Cw, P_FBD)) { sbspm = HUNDRED; }
                break;
            case DEAD_BAND:
                if (w.getPowerRatio() <= actualPowerRatios[1] && actualPowerRatios[1] <= v.getPowerRatio()) { sbspm = HUNDRED; }
                break;
            case FIRST_BAND_CHARGE:
                Point P_FBC = new Point(new BigDecimal(frequencies[1]), new BigDecimal(actualPowerRatios[1]));
                if (AreaUtils.isInTriangle(P_Dv, P_Dw, P_Ex, P_FBC)) { sbspm = HUNDRED; }
                break;
            case FULL_BAND_INPUT:
                if (actualPowerRatios[1] == powerRatio) { sbspm = HUNDRED; }
                break;
            default:
                log.error("Undefined zone [zone=" + whichZone + "]");
                break;
        }
        if (sbspm == null) {
            //TODO: check if DEAD BAND need to consider this relationship or not
            if (whichZone != DEAD_BAND && !isInverseRatioRelationship(frequencies, actualPowerRatios)) { sbspm = BigDecimal.ZERO; } else {
                Double mostProximatePowerRatio = getMostProximatePowerRatio(frequencies[1], actualPowerRatios[1], powerRatio);
                sbspm = new BigDecimal(100.0 - Math.abs(actualPowerRatios[1] - mostProximatePowerRatio));
            }
        }
        //		return sbspm;
        return sbspm.setScale(0, RoundingMode.HALF_UP);
    }

    @Autowired
    private SbspmParameter sbspmParameter;

    public BigDecimal calculateSBSPM() {
        if (frequencies.length != 2 || actualPowerRatios.length != 2) { return null; }

        Double powerRatio = service.run(frequencies[1], DefaultStrategy.SOC_BASELINE);
        ZoneEnum whichZone = ZoneEnum.of(frequencies[1]);
        if (powerRatio == null || whichZone == UNDEFINED_ZONE) { return null; }

        BigDecimal sbspm = null;
        Double sbspmN = 0.0;
        Double sbspmH = 0.0;
        Double sbspmL = 0.0;
        Double sbspmMax = 0.0;

        //Slope
        Double fullyChargeSlope;
        Double halfChargeNSlope;
        Double halfChargeHSlope;
        Double halfChargeLSlope;

        Double fullyDischargeSlope;
        Double halfDischargeNSlope;
        Double halfDischargeHSlope;
        Double halfDischargeLSlope;

        Double freqA = sbspmParameter.getParamA().getFreq();
        Double freqB = sbspmParameter.getParamB().getFreq();
        Double freqC = sbspmParameter.getParamC().getFreq();
        Double freqD = sbspmParameter.getParamD().getFreq();
        Double freqE = sbspmParameter.getParamE().getFreq();
        Double freqF = sbspmParameter.getParamF().getFreq();

        Double normalPowerA = sbspmParameter.getParamA().getNormalPower();
        Double normalPowerB = sbspmParameter.getParamB().getNormalPower();
        Double normalPowerC = sbspmParameter.getParamC().getNormalPower();
        Double normalPowerD = sbspmParameter.getParamD().getNormalPower();
        Double normalPowerE = sbspmParameter.getParamE().getNormalPower();
        Double normalPowerF = sbspmParameter.getParamF().getNormalPower();

        Double highPowerA = sbspmParameter.getParamA().getHighPower();
        Double highPowerB = sbspmParameter.getParamB().getHighPower();
        Double highPowerC = sbspmParameter.getParamC().getHighPower();
        Double highPowerD = sbspmParameter.getParamD().getHighPower();
        Double highPowerE = sbspmParameter.getParamE().getHighPower();
        Double highPowerF = sbspmParameter.getParamF().getHighPower();

        Double lowPowerA = sbspmParameter.getParamA().getLowPower();
        Double lowPowerB = sbspmParameter.getParamB().getLowPower();
        Double lowPowerC = sbspmParameter.getParamC().getLowPower();
        Double lowPowerD = sbspmParameter.getParamD().getLowPower();
        Double lowPowerE = sbspmParameter.getParamE().getLowPower();
        Double lowPowerF = sbspmParameter.getParamF().getLowPower();

        Double targetPowerNormal = 0.0;
        Double targetPowerH = 0.0;
        Double targetPowerL = 0.0;

        fullyChargeSlope = (normalPowerE - normalPowerF) / (freqE - freqF);
        halfChargeNSlope = (normalPowerD - normalPowerE) / (freqD - freqE);
        halfChargeHSlope = (highPowerD - highPowerE) / (freqD - freqE);
        halfChargeLSlope = (lowPowerD - lowPowerE) / (freqD - freqE);

        fullyDischargeSlope = (normalPowerA - normalPowerB) / (freqA - freqB);
        halfDischargeNSlope = (normalPowerB - normalPowerC) / (freqB - freqC);
        halfDischargeHSlope = (highPowerB - highPowerC) / (freqB - freqC);
        halfDischargeLSlope = (lowPowerB - lowPowerC) / (freqB - freqC);

        //計算 tagerPower = (currentFreq-F0)*slope+P0

        if (frequencies[0] <= freqA || frequencies[0] >= freqF) { //<=59.5  or >=60.5

            if (frequencies[0] <= freqA) {

                targetPowerNormal = 100.0;
                targetPowerH = 100.0;
                targetPowerL = 100.0;

            } else {

                targetPowerNormal = -100.0;
                targetPowerH = -100.0;
                targetPowerL = -100.0;

            }

        } else if (frequencies[0] > freqA && frequencies[0] <= freqB) { // 59.75 >= freq >  59.50

            targetPowerNormal = (frequencies[0] - freqB) * fullyDischargeSlope + normalPowerB;
            targetPowerH = targetPowerNormal;
            targetPowerL = targetPowerNormal;

        } else if (frequencies[0] > freqB && frequencies[0] < freqC) { // 59.98 >  freq >  59.75

            targetPowerNormal = (frequencies[0] - freqC) * halfDischargeNSlope + normalPowerC;
            targetPowerH = (frequencies[0] - freqC) * halfDischargeHSlope + highPowerC;
            targetPowerL = (frequencies[0] - freqC) * halfDischargeLSlope + lowPowerC;

        } else if (frequencies[0] >= freqC && frequencies[0] <= freqD) { // 60.02 >= freq >= 59.98

            targetPowerNormal = 0.0;
            targetPowerH = highPowerC;
            targetPowerL = lowPowerC;

        } else if (frequencies[0] > freqD && frequencies[0] < freqE) { // 60.25 >  freq >  60.02

            targetPowerNormal = (frequencies[0] - freqE) * halfChargeNSlope + normalPowerE;
            targetPowerH = (frequencies[0] - freqE) * halfChargeHSlope + highPowerE;
            targetPowerL = (frequencies[0] - freqE) * halfChargeLSlope + lowPowerE;

        } else if (frequencies[0] >= freqE && frequencies[0] < freqF) { // 60.50 >  freq >= 60.25

            targetPowerNormal = (frequencies[0] - freqF) * fullyChargeSlope + normalPowerF;
            targetPowerH = targetPowerNormal;
            targetPowerL = targetPowerNormal;
        }

        if (targetPowerNormal >= 100 && actualPowerRatios[1] >= 100) {

            sbspmN = 100.0;
            sbspmH = 100.0;
            sbspmL = 100.0;

        } else if (targetPowerNormal <= -100 && actualPowerRatios[1] <= -100) {

            sbspmN = 100.0;
            sbspmH = 100.0;
            sbspmL = 100.0;

        } else if (actualPowerRatios[1] <= targetPowerH && actualPowerRatios[1] >= targetPowerL) {

            sbspmN = 100.0;
            sbspmH = 100.0;
            sbspmL = 100.0;

        } else {

            sbspmN = 100 - Math.abs(actualPowerRatios[1] - targetPowerNormal);
            sbspmH = 100 - Math.abs(actualPowerRatios[1] - targetPowerH);
            sbspmL = 100 - Math.abs(actualPowerRatios[1] - targetPowerL);

        }


        sbspmMax = Math.max(sbspmN, sbspmH);
        sbspmMax = Math.max(sbspmMax, sbspmL);

        //判斷斜率是否正確
        Double deltaSlope = (actualPowerRatios[0] - actualPowerRatios[1]) * (frequencies[0] - frequencies[1]);

        if (sbspmMax < 0) {
        // if (deltaSlope > 0 || sbspmMax < 0) {
            sbspmMax = 0.0;
        }

        sbspm = new BigDecimal(sbspmMax).setScale(3, BigDecimal.ROUND_HALF_UP);
        return sbspm.setScale(0, RoundingMode.HALF_UP);
    }


    private Double getMostProximatePowerRatio(Double frequency, Double actualPowerRatio, Double powerRatio) {
        Double mostProximatePowerRatio = null;
        ZoneEnum whichZone = ZoneEnum.of(frequency);
        switch (whichZone) {
            case FULL_BAND_OUTPUT:
                mostProximatePowerRatio = powerRatio;
                break;
            case FIRST_BAND_DISCHARGE:
                double[] x_BC_uv = {B.getFrequency(), getAverage(B, C), C.getFrequency()};
                double[] y_BC_uv = {u.getPowerRatio(), getAverage(u, v), v.getPowerRatio()};
                double FBD_p1 = getFractionDigits(Interpolation.calculate(x_BC_uv, y_BC_uv, frequency), 2);

                double[] x_BC_uw = {B.getFrequency(), getAverage(B, C), C.getFrequency()};
                double[] y_BC_uw = {u.getPowerRatio(), getAverage(u, w), w.getPowerRatio()};
                double FBD_p2 = getFractionDigits(Interpolation.calculate(x_BC_uw, y_BC_uw, frequency), 2);

                double FBD_gap1 = Math.abs(actualPowerRatio - FBD_p1);
                double FBD_gap2 = Math.abs(actualPowerRatio - FBD_p2);
                double FBD_minGap = Math.min(FBD_gap1, FBD_gap2);

                mostProximatePowerRatio = (FBD_minGap == FBD_gap1) ? FBD_p1 : FBD_p2;
                break;
            case DEAD_BAND:
                double DB_gap1 = Math.abs(actualPowerRatio - v.getPowerRatio());
                double DB_gap2 = Math.abs(actualPowerRatio - w.getPowerRatio());
                double DB_minGap = Math.min(DB_gap1, DB_gap2);

                mostProximatePowerRatio = (DB_minGap == DB_gap1) ? v.getPowerRatio() : w.getPowerRatio();
                break;
            case FIRST_BAND_CHARGE:
                double[] x_DE_vx = {D.getFrequency(), getAverage(D, E), E.getFrequency()};
                double[] y_DE_vx = {v.getPowerRatio(), getAverage(v, x), x.getPowerRatio()};
                double FBC_p1 = getFractionDigits(Interpolation.calculate(x_DE_vx, y_DE_vx, frequency), 2);

                double[] x_DE_wx = {D.getFrequency(), getAverage(D, E), E.getFrequency()};
                double[] y_DE_wx = {w.getPowerRatio(), getAverage(w, x), x.getPowerRatio()};
                double FBC_p2 = getFractionDigits(Interpolation.calculate(x_DE_wx, y_DE_wx, frequency), 2);

                double FBC_gap1 = Math.abs(actualPowerRatio - FBC_p1);
                double FBC_gap2 = Math.abs(actualPowerRatio - FBC_p2);
                double FBC_minGap = Math.min(FBC_gap1, FBC_gap2);

                mostProximatePowerRatio = (FBC_minGap == FBC_gap1) ? FBC_p1 : FBC_p2;
                break;
            case FULL_BAND_INPUT:
                mostProximatePowerRatio = powerRatio;
                break;
            default:
                log.error("Undefined zone [zone=" + whichZone + "]");
                break;
        }
        return mostProximatePowerRatio;
    }
}
