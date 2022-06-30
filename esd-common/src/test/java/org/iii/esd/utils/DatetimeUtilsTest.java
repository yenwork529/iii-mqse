package org.iii.esd.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource("classpath:application.yml")
@Log4j2
class DatetimeUtilsTest {

    @Test
    void testParseDateString() {

    }

    @Test
    void testParseDateStringSimpleDateFormat() {

    }

    @Test
    void testTruncated() {

    }

    @Test
    void testGetFirstHourOfDay() {

    }

    @Test
    void testAdd() {

    }

    @Test
    void testIsLunarHoliday() {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(2020, 0, 24);
        log.info(DatetimeUtils.isLunarHoliday(c.getTime()));
    }

    @Test
    void testIsNationalHoliday() {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(2020, 1, 28);
        log.info(DatetimeUtils.isNationalHoliday(c.getTime()));
        log.info(c.getTime().compareTo(new Date()));
    }

    @Test
    void testFormatDate() {
        Date date = new Date();
        log.info(DatetimeUtils.toISOFormat(date));
    }

    @Test
    void testDiffSeconds() {
        Instant t1 = Instant.now();
        Instant t2 = t1.plusSeconds(60);

        long diff1 = DatetimeUtils.diffSeconds(t1, t2);
        long diff2 = DatetimeUtils.diffSeconds(t2, t1);

        assertThat(diff1)
                  .isEqualTo(diff2)
                  .isEqualTo(60);
    }

    @Test
    void testDiffMinutes(){
        Instant t1 = Instant.now();
        Instant t2 = t1.plusSeconds(10 * 60);

        long diff1 = DatetimeUtils.diffMinutes(t1, t2);
        long diff2 = DatetimeUtils.diffMinutes(t2, t1);

        assertThat(diff1)
                  .isEqualTo(diff2)
                  .isEqualTo(10);
    }

    @Test
    void testToLocalDateTime(){
        long timestamp = System.currentTimeMillis();
        Date date = new Date(timestamp);
        Instant instant = date.toInstant();

        LocalDateTime fromTimestamp = DatetimeUtils.toLocalDateTime(timestamp);
        LocalDateTime fromDate = DatetimeUtils.toLocalDateTime(date);
        LocalDateTime fromInstant = DatetimeUtils.toLocalDateTime(instant);

        assertThat(fromTimestamp).isEqualTo(fromDate);
        assertThat(fromTimestamp).isEqualTo(fromInstant);
        assertThat(fromDate).isEqualTo(fromInstant);
    }
}
