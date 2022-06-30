package org.iii.esd.server.def;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum LoadPattern {

    M0("M0_KW", "原始負載(M0)", "KW", 1),
    M1("M1_KW", "負載(M1)", "KW", 2);

    private String type;

    private String name;

    private String unit;

    private int order;

    public static LoadPattern getCode(String type) {
        for (LoadPattern pattern : values()) {
            if (pattern.getType().equals(type)) {
                return pattern;
            }
        }
        return null;
    }
}
