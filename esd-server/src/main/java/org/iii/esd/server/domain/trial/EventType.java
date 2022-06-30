package org.iii.esd.server.domain.trial;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

import org.iii.esd.exception.EnumInitException;

@Getter
public enum EventType {
    TYPE_A("NULL", "RESPONSE_BEGIN", "RESPONSE_DONE", "RESPONSE_END"),
    TYPE_B(),
    TYPE_C("START_SERVICE", "STOP_SERVICE", "START_STAND_BY", "STOP_STAND_BY");

    private Set<String> relativeReacts;

    EventType() {
        this.relativeReacts = Collections.emptySet();
    }

    EventType(String... reactTypes) {
        this.relativeReacts = new HashSet<>(Arrays.asList(reactTypes));
    }

    public static EventType ofName(String name) {
        for (EventType value : EventType.values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }

        throw new EnumInitException(EventType.class, name);
    }
}
