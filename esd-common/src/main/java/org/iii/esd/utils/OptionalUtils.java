package org.iii.esd.utils;

import java.util.Optional;

public final class OptionalUtils {
    private OptionalUtils() {}

    public static <T> T or(T target, T defaut) {
        return Optional.ofNullable(target)
                       .orElse(defaut);
    }
}
