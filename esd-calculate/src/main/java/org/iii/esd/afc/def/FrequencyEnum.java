package org.iii.esd.afc.def;

public enum FrequencyEnum {

    A("A", 59.50),
    B("B", 59.75),
    C("C", 59.98),
    O("O", 60.00),
    D("D", 60.02),
    E("E", 60.25),
    F("F", 60.50),
    UNDEFINED_FREQUENCY("?", null),
    ;

    // X-axis: A,B,C,D,E,F
    private String refPoint;
    // Hertz(Hz)
    private Double frequency;

    private FrequencyEnum(String refPoint, Double frequency) {
        this.refPoint = refPoint;
        this.frequency = frequency;
    }

    public static FrequencyEnum of(Double frequency) {
        for (FrequencyEnum entity : values()) {
            Double entityFrequency = entity.getFrequency();

            if (entityFrequency != null) {
                if (entityFrequency.equals(frequency)) { return entity; }
            }
        }
        return UNDEFINED_FREQUENCY;
    }

    public String getRefPoint() {
        return refPoint;
    }

    public Double getFrequency() {
        return frequency;
    }
}
