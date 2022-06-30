package org.iii.esd.mongo.enums;

public enum ResourceType {
    dr("DR", 1),
    cgen("CGEN", 2),
    ugen("UGEN", 3),
    gess("GESS", 4),
    notSuported("notSupported", 5);

    private String name;
    private Integer code;

    ResourceType(String name, Integer code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public Integer getCode() {
        return this.code;
    }

    public static ResourceType ofCode(Integer code) {
        ResourceType[] var1 = values();

        for (ResourceType type : var1) {
            if (type.code.equals(code)) {
                return type;
            }
        }

        return notSuported;
    }
}
