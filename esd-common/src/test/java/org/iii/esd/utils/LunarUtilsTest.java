package org.iii.esd.utils;

import java.util.Calendar;
import java.util.Date;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import org.iii.esd.Constants;

@TestPropertySource("classpath:application.yml")
@Log4j2
class LunarUtilsTest {

    @Test
    void testLunar2Date() {
        int nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;
        log.info("明年春節是{}", Constants.DATE_FORMAT.format(LunarUtils.lunar2Date(nextYear, 1, 1, false).get()));
        log.info("明年端午是{}", Constants.DATE_FORMAT.format(LunarUtils.lunar2Date(nextYear, 5, 5, false).get()));
        log.info("明年中秋是{}", Constants.DATE_FORMAT.format(LunarUtils.lunar2Date(nextYear, 8, 15, false).get()));
    }

    @Test
    void testDate2Lunar() {
        Calendar c = Calendar.getInstance();
        c.set(1980, 3, 2);
        Lunar lunar = LunarUtils.date2Lunar(c.getTime()).get();
        log.info("國曆1980年4月2日->農曆" + LunarUtils.getLunarExpress(lunar));
        c.set(1981, 3, 6);
        lunar = LunarUtils.date2Lunar(c.getTime()).get();
        log.info("國曆1981年4月6日->農曆" + LunarUtils.getLunarExpress(lunar));
        lunar = LunarUtils.date2Lunar(new Date()).get();
        log.info("今日是農曆" + LunarUtils.getLunarExpress(lunar));
    }

    @Test
    void testAnimalsYear() {
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        log.info("今年是{}年", LunarUtils.animalsYear(thisYear));
    }

    @Test
    void testCyclical() {
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        log.info("今年是{}年", LunarUtils.cyclical(thisYear));
    }

    @Test
    void testGetPrevLunar() {
        int nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;
        log.info("明年春節是{}", Constants.DATE_FORMAT.format(LunarUtils.lunar2Date(nextYear, 1, 1, false).get()));
        Lunar lunar = LunarUtils.getPrevLunar(nextYear, 1, 1, false).get();
        log.info("今年除夕是" + LunarUtils.getLunarExpress(lunar));
    }

    @Test
    void testLeapMonth() {
        for (int y = 1900; y <= 2099; y++) {
            int month = LunarUtils.leapMonth(y);
            log.info(month == 0 ? y + "年沒有潤月" : "{}年的潤月是{}月", y, LunarUtils.leapMonth(y));
        }
    }

}