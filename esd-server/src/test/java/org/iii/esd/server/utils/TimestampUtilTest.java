package org.iii.esd.server.utils;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import org.iii.esd.utils.DatetimeUtils;

@Log4j2
public class TimestampUtilTest {

    @Test
    public void testGetTomorrowStart() {
        Instant tomorrowStart = DatetimeUtils.getTomorrowStart();
        log.info("tomorrow start: {}", tomorrowStart);
        log.info("tomorrow start local: {}", LocalDateTime.ofInstant(tomorrowStart, ZoneId.systemDefault()));
        log.info("tomorrow start date: {}", Date.from(tomorrowStart));
    }

    @Test
    public void testGetTomorrowEnd() {
        Instant tomorrowEnd = DatetimeUtils.getTomorrowEnd();
        log.info("tomorrow end: {}", tomorrowEnd);
        log.info("tomorrow end local: {}", LocalDateTime.ofInstant(tomorrowEnd, ZoneId.systemDefault()));
        log.info("tomorrow end date: {}", Date.from(tomorrowEnd));
    }
}
