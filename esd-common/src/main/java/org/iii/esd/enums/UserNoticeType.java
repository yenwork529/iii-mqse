package org.iii.esd.enums;

import lombok.Getter;

public enum UserNoticeType {

    EMAIL(1),
    PHONE(2),
    LINE(3);

    @Getter
    private long value;

    UserNoticeType(long value) {
        this.value = value;
    }

}
