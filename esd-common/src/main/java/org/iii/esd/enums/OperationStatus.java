package org.iii.esd.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OperationStatus {

    Unknown(null),
    /**
     * 正常
     */
    Normal(0),
    /**
     * 異常
     */
    Abnormal(1),
    ;

    private Integer status;

    public static OperationStatus getStatus(Integer status) {
        for (OperationStatus ostatus : values()) {
            if (ostatus.getStatus() == status) {
                return ostatus;
            }
        }
        return Unknown;
    }

}
