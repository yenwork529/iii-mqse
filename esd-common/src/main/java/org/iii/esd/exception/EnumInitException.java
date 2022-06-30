package org.iii.esd.exception;

import java.util.Arrays;
import java.util.stream.Collectors;

import lombok.Getter;

@Getter
public class EnumInitException extends RuntimeException {
    private Class<? extends Enum<?>> claz;
    private Object[] params;

    public EnumInitException(Class<? extends Enum<?>> claz, Object... params) {
        super(EnumInitException.buildMessage(claz, params));

        this.claz = claz;
        this.params = params;
    }

    private static String buildMessage(Class<? extends Enum<?>> claz, Object... params) {
        return String.format("enum {%1$s} init failed with [%2$s]", claz.getSimpleName(),
                Arrays.asList(params)
                      .stream()
                      .map(Object::toString)
                      .collect(Collectors.joining(",")));
    }
}
