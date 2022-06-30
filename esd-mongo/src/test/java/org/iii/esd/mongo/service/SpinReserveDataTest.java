package org.iii.esd.mongo.service;

import lombok.extern.log4j.Log4j2;
import org.iii.esd.Constants;
import org.iii.esd.enums.NoticeType;
import org.iii.esd.mongo.config.MongoDBConfig;
import org.iii.esd.mongo.document.SpinReserveData;
import org.iii.esd.utils.DatetimeUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

//    @SpringBootTest (classes = {
//            SpinReserveService.class,
//    })
//    @EnableAutoConfiguration
//    @Log4j2
//    @ContextConfiguration(classes = MongoDBConfig.class,
//            initializers = SpinReserveDataTest.Initializer.class)
public class SpinReserveDataTest {


    @Autowired
    private SpinReserveService service;

    static final DateFormat yyyyMMddHHmmss = Constants.TIMESTAMP_FORMAT2;

    @Test
    void testAddSpinReserveData() throws ParseException {
        Date end = yyyyMMddHHmmss.parse("22221212 22:00:00"); //new Date();

        SpinReserveData obj = new SpinReserveData();
        obj.setStartTime(end);
//         BigDecimal powerM1t2, powerCBLt2, powerCBLt3 = 1000,2000,3000;
//        obj.setPowerM1t2(new BigDecimal(1000));
//        obj.setPowerCBLt2(new BigDecimal(2000));
//        obj.setPowerCBLt3(new BigDecimal(3000));
        System.out.println(obj.toString());
        System.out.println("SpinReserveServiceService:"+service);
        service.saveSpinReserveData(obj);
    }
}
