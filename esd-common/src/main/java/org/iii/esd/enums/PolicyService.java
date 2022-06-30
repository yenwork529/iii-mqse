package org.iii.esd.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PolicyService {
    A(0), B(1), C(2), D(3), E(4), F(5),

    ;

    private int value;

    public int value() {
        return value;
    }

}
