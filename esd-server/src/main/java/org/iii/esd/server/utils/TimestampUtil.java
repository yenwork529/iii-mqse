package org.iii.esd.server.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public final class TimestampUtil {
    private TimestampUtil() {}

    public static Long millisFromSecond(long second) {
        return second * 1000;
    }

    public static Long secondFromMillis(long millis) {
        return millis / 1000;
    }
}
