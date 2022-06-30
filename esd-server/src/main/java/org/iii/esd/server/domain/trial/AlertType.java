package org.iii.esd.server.domain.trial;

import org.apache.commons.lang3.StringUtils;

import org.iii.esd.exception.EnumInitException;

public enum AlertType {
    CONSUME_NOT_ENOUGH;

    public static AlertType ofName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }

        for (AlertType value : AlertType.values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }

        throw new EnumInitException(AlertType.class, name);
    }
}
