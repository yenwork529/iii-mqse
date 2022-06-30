package org.iii.esd.server.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.stream.Stream;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import org.iii.esd.utils.DatetimeUtils;

@Log4j2
public class DateTest {
    @Test
    public void testLocalDateRange() {
        LocalDate thisMonth = LocalDate.now();
        LocalDate monthStart = LocalDate.of(thisMonth.getYear(), thisMonth.getMonth(), 1);
        LocalDate nextStart = monthStart.plusMonths(1);

        Stream.iterate(monthStart, d -> d.isBefore(nextStart), d -> d.plusDays(1))
              .forEach(date -> {
                  log.info("date {}", date);
              });
    }

    @Test
    public void testDefaultTime(){
        Instant defaultEpoch = DatetimeUtils.DEFAULT_EPOCH;
        Instant defaultEternal = DatetimeUtils.DEFAULT_ETERNAL;

        log.info("epoch {}", defaultEpoch);
        log.info("eternal {}", defaultEternal);
    }
}
