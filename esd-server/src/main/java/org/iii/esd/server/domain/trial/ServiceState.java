package org.iii.esd.server.domain.trial;

import org.apache.commons.lang3.StringUtils;

import org.iii.esd.exception.EnumInitException;

public enum ServiceState {
    START,
    STOP,
    ABANDON;

    public static ServiceState ofName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }

        for (ServiceState value : values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }

        throw new EnumInitException(ServiceState.class, name);
    }
}
