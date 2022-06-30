package org.iii.esd.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PolicyDevice {

    PV(0),    // 太陽能
    ESS(1), // 電池
    CL(2),    // 可控設備
    GE(3),    // 發電機
    ;

    private int value;

    public int value() {
        return value;
    }
}
