package org.iii.esd.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Listener {

    SERVER(60001),
    THINCLIENT(60002),
    THINCLIENT2(60003),
    THINCLIENT3(60004),
    THINCLIENT4(60005),
    THINCLIENT5(60006),
    AUTH(60010),
    DNP(60020),
    MONITOR_A(61001),
    MONITOR_B(61002),
    ;

    private int port;

    public static Listener getListener(int port) {
        for (Listener listener : values()) {
            if (listener.getPort() == port) {
                return listener;
            }
        }
        return null;
    }

}