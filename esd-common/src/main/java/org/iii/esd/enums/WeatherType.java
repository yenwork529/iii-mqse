package org.iii.esd.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WeatherType {

    actually(1),
    forecast(2),
    ;
    private int code;

    public static WeatherType getCode(int code) {
        for (WeatherType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }

}