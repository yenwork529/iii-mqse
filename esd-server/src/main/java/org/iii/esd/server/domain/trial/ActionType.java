package org.iii.esd.server.domain.trial;

import org.apache.commons.lang3.StringUtils;

import org.iii.esd.exception.EnumInitException;

public enum ActionType {
    BEGIN,
    RUNNING,
    END,
    DONE;

    public static ActionType ofName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }

        for (ActionType value : ActionType.values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }

        throw new EnumInitException(ActionType.class, name);
    }
}
