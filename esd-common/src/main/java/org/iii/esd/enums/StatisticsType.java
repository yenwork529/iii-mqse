package org.iii.esd.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatisticsType {

    min1(60),
    min3(180),
    min15(900),
    hour(3600),
    day(86400),
    month(2592000),
    ;

    private int sec;

    public static StatisticsType getSec(int sec) {
        for (StatisticsType type : values()) {
            if (type.getSec() == sec) {
                return type;
            }
        }
        return null;
    }

}