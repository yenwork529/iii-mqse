package org.iii.esd.afc.def;

public enum PowerRatioEnum {

    t("t", 100.0),  //放電(discharge)
    u("u", 48.0),
    v("v", 9.0),
    o("o", 0.0),
    w("w", -9.0),
    x("x", -48.0),
    y("y", -100.0),  //充電(charge)
    UNDEFINED_POWER("?", null),
    ;

    // Y-axis: t,u,v,o,w,y,z
    private String refPoint;
    // Power Ratio: percentage(%)
    private Double powerRatio;

    private PowerRatioEnum(String refPoint, Double power) {
        this.refPoint = refPoint;
        this.powerRatio = power;
    }

    public static PowerRatioEnum of(Double power) {
        for (PowerRatioEnum entity : values()) {
            Double entityPower = entity.getPowerRatio();

            if (entityPower != null) {
                if (entityPower.equals(power)) { return entity; }
            }
        }
        return UNDEFINED_POWER;
    }

    public String getRefPoint() {
        return refPoint;
    }

    public Double getPowerRatio() {
        return powerRatio;
    }
}
