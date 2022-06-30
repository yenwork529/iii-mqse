package org.iii.esd.server.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Log4j2
public class BeanUtil {
    public static Set<String> filterProperty(Object obj) {
        try {
            Map<String, Object> props = PropertyUtils.describe(obj);
            return props.entrySet()
                        .stream()
                        .filter(entry -> (entry.getValue() instanceof Boolean)
                                && ((boolean) entry.getValue()))
                        .map(entry -> entry.getKey())
                        .collect(Collectors.toSet());
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return Collections.emptySet();
        }
    }
}
