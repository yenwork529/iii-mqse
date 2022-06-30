package org.iii.esd.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnableStatus {

    disable(0),
    enable(1),
    ;

    private int status;

    public static EnableStatus getStatus(int status) {
        for (EnableStatus enableStatus : values()) {
            if (enableStatus.getStatus() == status) {
                return enableStatus;
            }
        }
        return null;
    }

    public static boolean isEnabled(EnableStatus status) {
        return EnableStatus.enable.equals(status);
    }

    public static boolean isNotEnabled(EnableStatus status) {
        return !isEnabled(status);
    }

    public static boolean isDisabled(EnableStatus status) {
        return EnableStatus.disable.equals(status);
    }

    public static boolean isNotDisabled(EnableStatus status) {
        return !isDisabled(status);
    }
}
