package org.iii.esd.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public final class TimestampUtils {
    private TimestampUtils() {}

    public static long getAoTimestamp(Instant time) {
        long millis = time.toEpochMilli();
        long seconds = millis / 1000;
        return seconds * 1000;
    }

    public static final String ZONE_ID = "UTC+8";
    public static final int ZONE_OFFSET = 8;

    public static Instant truncateToMinute(Instant time) {
        LocalDateTime ldt = LocalDateTime.ofInstant(time, ZoneId.of(ZONE_ID))
                                         .truncatedTo(ChronoUnit.MINUTES);

        return ldt.toInstant(ZoneOffset.ofHours(ZONE_OFFSET));
    }
}
