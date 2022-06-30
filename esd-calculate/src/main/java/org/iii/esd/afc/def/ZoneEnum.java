package org.iii.esd.afc.def;

import static org.iii.esd.afc.def.FrequencyEnum.A;
import static org.iii.esd.afc.def.FrequencyEnum.B;
import static org.iii.esd.afc.def.FrequencyEnum.C;
import static org.iii.esd.afc.def.FrequencyEnum.D;
import static org.iii.esd.afc.def.FrequencyEnum.E;
import static org.iii.esd.afc.def.FrequencyEnum.F;
import static org.iii.esd.afc.def.FrequencyEnum.UNDEFINED_FREQUENCY;

public enum ZoneEnum {

    FULL_BAND_OUTPUT("FULL_BAND_OUTPUT", "FBO", A, B),  //全輸出反應頻率
    FIRST_BAND_DISCHARGE("FIRST_BAND_DISCHARGE", "FBD", B, C),  //第一段反應頻率(放電)
    DEAD_BAND("DEAD_BAND", "DBZ", C, D),  //不動帶(DEAD BAND)
    FIRST_BAND_CHARGE("FIRST_BAND_CHARGE", "FBC", D, E),  //第一段反應頻率(充電)
    FULL_BAND_INPUT("FULL_BAND_INPUT", "FBI", E, F),  //全輸入反應頻率
    UNDEFINED_ZONE("UNDEFINED", "UDF", UNDEFINED_FREQUENCY, UNDEFINED_FREQUENCY),  //未定義區域
    ;

    private String name;
    private String code;
    // Hertz(Hz)
    private FrequencyEnum lowerLimit;
    // Hertz(Hz)
    private FrequencyEnum upperLimit;

    private ZoneEnum(String name, String code, FrequencyEnum lowerLimit, FrequencyEnum upperLimit) {
        this.name = name;
        this.code = code;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    public static ZoneEnum of(Double frequency) {
        for (ZoneEnum entity : values()) {
            Double entityLowerLimitFrequency = entity.getLowerLimit().getFrequency();
            Double entityUpperLimitFrequency = entity.getUpperLimit().getFrequency();

            if (entityLowerLimitFrequency != null && entityUpperLimitFrequency != null) {
                if (frequency < FrequencyEnum.O.getFrequency()) {
                    if (frequency >= entityLowerLimitFrequency && frequency < entityUpperLimitFrequency) { return entity; }
                } else if (frequency > FrequencyEnum.O.getFrequency()) {
                    if (frequency > entityLowerLimitFrequency && frequency <= entityUpperLimitFrequency) { return entity; }
                } else {
                    return DEAD_BAND;
                }
            }
        }
        return UNDEFINED_ZONE;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public FrequencyEnum getLowerLimit() {
        return lowerLimit;
    }

    public FrequencyEnum getUpperLimit() {
        return upperLimit;
    }
}
