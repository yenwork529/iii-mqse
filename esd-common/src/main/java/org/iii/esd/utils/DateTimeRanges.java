package org.iii.esd.utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

public class DateTimeRanges {

    public static synchronized LocalDateTimeRange ofLocalDateTime(LocalDateTime start, LocalDateTime stop) {
        return new LocalDateTimeRange(start, stop);
    }

    public static class LocalDateTimeRange {

        private LocalDateTime start;
        private LocalDateTime stop;

        protected LocalDateTimeRange(LocalDateTime start, LocalDateTime stop) {
            this.start = start;
            this.stop = stop;
        }

        public Stream<LocalDateTime> byMinutes() {
            return Stream.iterate(start, d -> d.plusMinutes(1))
                         .limit(ChronoUnit.MINUTES.between(start, stop) + 1);
        }

        public Stream<LocalDateTime> bySeconds() {
            return Stream.iterate(start, d -> d.plusSeconds(1))
                         .limit(ChronoUnit.SECONDS.between(start, stop) + 1);
        }
    }
}
