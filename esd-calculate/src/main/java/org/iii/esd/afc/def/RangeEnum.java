package org.iii.esd.afc.def;

import static org.iii.esd.afc.def.PowerRatioEnum.UNDEFINED_POWER;
import static org.iii.esd.afc.def.PowerRatioEnum.o;
import static org.iii.esd.afc.def.PowerRatioEnum.t;
import static org.iii.esd.afc.def.PowerRatioEnum.u;
import static org.iii.esd.afc.def.PowerRatioEnum.v;
import static org.iii.esd.afc.def.PowerRatioEnum.w;
import static org.iii.esd.afc.def.PowerRatioEnum.x;
import static org.iii.esd.afc.def.PowerRatioEnum.y;

public enum RangeEnum {

    RANGE_t_u("tu", "tu", t, u),
    RANGE_u_o("uo", "uo", u, o),
    RANGE_u_v("uv", "uv", u, v),
    RANGE_u_w("uw", "uw", u, w),
    RANGE_v_w("vw", "vw", v, w),
    RANGE_v_x("vx", "vx", v, x),
    RANGE_w_x("wx", "wx", w, x),
    RANGE_o_x("oy", "oy", o, x),
    RANGE_x_y("xy", "xy", x, y),
    UNDEFINED_RANGE("??", "??", UNDEFINED_POWER, UNDEFINED_POWER),
    ;

    private String name;
    private String code;
    // Power Ratio: percentage(%)
    private PowerRatioEnum lowerLimit;
    // Power Ratio: percentage(%)
    private PowerRatioEnum upperLimit;

    private RangeEnum(String name, String code, PowerRatioEnum upperLimit, PowerRatioEnum lowerLimit) {
        this.name = name;
        this.code = code;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    @Deprecated
    public static RangeEnum of(Double powerRatio) {
        for (RangeEnum entity : values()) {
            Double entityLowerLimitPower = entity.getLowerLimit().getPowerRatio();
            Double entityUpperLimitPower = entity.getUpperLimit().getPowerRatio();

            if (entityLowerLimitPower != null && entityUpperLimitPower != null) {
                if (powerRatio >= entityLowerLimitPower && powerRatio <= entityUpperLimitPower) { return entity; }
            }
        }
        return UNDEFINED_RANGE;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public PowerRatioEnum getLowerLimit() {
        return lowerLimit;
    }

    public PowerRatioEnum getUpperLimit() {
        return upperLimit;
    }
}
