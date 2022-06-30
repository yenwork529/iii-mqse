package org.iii.esd.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NoticeType {

    ALERT(1),
    UNLOAD(2),
    ;

    private int value;

    public static NoticeType getValue(int value) {
        for (NoticeType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }

}