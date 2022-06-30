package org.iii.esd.mongo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MeasureType {

    SBSPM(1, "sbspm", "Second By Second Performance Measure"),
    SPM(2, "spm", "Service Performance Measure"),
    ASPM(3, "aspm", "Annual Service Performance Measure"),
    ;

    private Integer code;

    private String shortName;

    private String fullName;

}
