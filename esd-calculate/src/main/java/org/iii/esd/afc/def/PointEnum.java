package org.iii.esd.afc.def;

import java.math.BigDecimal;

import static org.iii.esd.afc.def.FrequencyEnum.B;
import static org.iii.esd.afc.def.FrequencyEnum.C;
import static org.iii.esd.afc.def.FrequencyEnum.D;
import static org.iii.esd.afc.def.FrequencyEnum.E;
import static org.iii.esd.afc.def.PowerRatioEnum.u;
import static org.iii.esd.afc.def.PowerRatioEnum.v;
import static org.iii.esd.afc.def.PowerRatioEnum.w;
import static org.iii.esd.afc.def.PowerRatioEnum.x;

public enum PointEnum {

    Bu(new BigDecimal(B.getFrequency()), new BigDecimal(u.getPowerRatio())),
    Cv(new BigDecimal(C.getFrequency()), new BigDecimal(v.getPowerRatio())),
    Cw(new BigDecimal(C.getFrequency()), new BigDecimal(w.getPowerRatio())),
    Dv(new BigDecimal(D.getFrequency()), new BigDecimal(v.getPowerRatio())),
    Dw(new BigDecimal(D.getFrequency()), new BigDecimal(w.getPowerRatio())),
    Ex(new BigDecimal(E.getFrequency()), new BigDecimal(x.getPowerRatio())),
    ;

    // X-axis: A,B,C,D,E,F
    private BigDecimal frequency;
    // Y-axis: t,u,v,w,x,y
    private BigDecimal powerRatio;

    private PointEnum(BigDecimal frequency, BigDecimal powerRatio) {
        this.frequency = frequency;
        this.powerRatio = powerRatio;
    }

    public BigDecimal getFrequency() {
        return frequency;
    }

    public BigDecimal getPowerRatio() {
        return powerRatio;
    }
}
