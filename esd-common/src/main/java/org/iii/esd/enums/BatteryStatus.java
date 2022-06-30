package org.iii.esd.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BatteryStatus {

    Unknown(null),
    /**
     * 待機
     */
    Standby(0),
    /**
     * 充電
     */
    Charge(1),
    /**
     * 放電
     */
    Discharge(2),
    ;

    private Integer status;

    public static BatteryStatus getStatus(Integer status) {
        for (BatteryStatus batteryStatus : values()) {
            if (batteryStatus.getStatus() == status) {
                return batteryStatus;
            }
        }
        return Unknown;
    }

}