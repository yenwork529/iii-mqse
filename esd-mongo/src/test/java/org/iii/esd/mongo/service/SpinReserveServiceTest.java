package org.iii.esd.mongo.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.enums.NoticeType;
import org.iii.esd.mongo.document.SiloCompanyProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.SpinReserveData;
import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.utils.DatetimeUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {
        SpinReserveService.class,
        QueryService.class,
        StatisticsService.class,
        UpdateService.class,
})
@EnableAutoConfiguration
@Log4j2
class SpinReserveServiceTest extends AbstractServiceTest {

    @Value("${companyId}")
    private Long companyId;

    @Value("${srId}")
    private Long srId;

    @Autowired
    private SpinReserveService service;

    @Test
    @Disabled
    void testAddSpinReserveProfile() {
        SpinReserveProfile obj = new SpinReserveProfile();
        obj.setName("馬雅資訊-SR");
        long seq = service.addSpinReserveProfile(obj);
        log.info(seq);
    }

    @Test
    @Disabled
    void testUpdateSpinReserveProfile() {
        Optional<SpinReserveProfile> opt = service.findSpinReserveProfile(srId);
        if (opt.isPresent()) {
            SpinReserveProfile obj = opt.get();
            String oldName = obj.getName();
            String newName = "III-SRTest";
            obj.setName(newName);
            obj.setSiloCompanyProfile(new SiloCompanyProfile(1l));
            obj.setBidContractCapacity(3000);
            service.updateSpinReserveProfile(obj);
            obj = service.findSpinReserveProfile(srId).get();
            assertEquals(obj.getName(), newName);
            obj.setName(oldName);
            service.updateSpinReserveProfile(obj);
        } else {
            fail();
        }
    }

    @Test
    @Disabled
    void testFindSpinReserveProfile() {
        Optional<SpinReserveProfile> opt = service.findSpinReserveProfile(srId);
        if (opt.isPresent()) {
            log.info(opt.get().toString());
        } else {
            fail();
        }
    }

    @Test
    @Disabled
    void testFindAllSpinReserveProfile() {
        List<SpinReserveProfile> list = service.findAllSpinReserveProfile();
        log.info(list.size());
        list.forEach(obj -> log.info(obj.toString()));
    }

    @Test
    @Disabled
    void testFindEnableSpinReserveProfile() {
        List<SpinReserveProfile> list = service.findEnableSpinReserveProfile();
        log.info(list.size());
        list.forEach(obj -> log.info(obj.toString()));
    }

    @Test
    @Disabled
    void testFindSpinReserveProfileByExample() {
        List<SpinReserveProfile> list = service.findSpinReserveProfileByCompanyIdAndEnableStatus(companyId, EnableStatus.enable);
        log.info(list.size());
        list.forEach(obj -> log.info(obj.toString()));
    }

    @Test
    @Disabled
    void testDeleteSpinReserveProfile() {
        SpinReserveProfile obj = new SpinReserveProfile();
        obj.setName("TEST");
        long seq = service.addSpinReserveProfile(obj);
        service.deleteSpinReserveProfile(seq);
        Optional<SpinReserveProfile> opt = service.findSpinReserveProfile(seq);
        if (opt.isPresent()) {
            fail();
        }
    }

    @Test
    @Disabled
    void testFindSpinReserveDataBySrIdAndNoticeTime() throws ParseException {
        Date start = yyyyMMddHHmmss.parse("20200220 00:00:00");
        Date end = yyyyMMddHHmmss.parse("20200221 00:00:00");

        List<SpinReserveData> list = service.findSpinReserveDataBySrIdAndNoticeTime(srId, start, end);
        for (SpinReserveData spinReserveData : list) {
            log.info(spinReserveData.toString());
        }
    }

    @Test
    @Disabled
    void testFindSpinReserveDataBySrIdAndNoticeTypeAndNoticeTim() throws ParseException {
        Date start = yyyyMMddHHmmss.parse("20200527 18:00:00");
        Date end = yyyyMMddHHmmss.parse("20200527 18:30:00");

        List<SpinReserveData> list = service.findSpinReserveDataBySrIdAndNoticeTypeAndNoticeTime(srId, NoticeType.UNLOAD, start, end);
        for (SpinReserveData spinReserveData : list) {
            log.info(spinReserveData.toString());
        }
    }

    @Test
    @Disabled
    void testCalculateFieldSpinReserveBidRatioBySridAndTime() throws ParseException {
        Date time = yyyyMMddHHmmss.parse("20201214 17:00:00");
        Map<Long, Double> map = service.calculateFieldSpinReserveBidRatioBySridAndTime(3l, time);
        log.info(map.size());
        log.info(map.get(3l));
    }

    @Test
    @Disabled
    void testCalculateBaseLine() throws ParseException {
        Long fieldId = 6l;
        Date noticeTime = yyyyMMddHHmmss.parse("20200919 17:44:19");
        log.info(noticeTime.getTime() % 60 * 1000 / 1000);

        Date _noticeTime = noticeTime.getTime() % (60 * 1000) / 1000 < 20 ?
                DatetimeUtils.add(noticeTime, Calendar.SECOND, -20) : noticeTime;
        BigDecimal value = service.calculateBaseLine(_noticeTime, fieldId);
        log.info(value);
        List<FieldProfile> fieldProfileList = Arrays.asList(new FieldProfile(fieldId));
        Map<Long, BigDecimal> baseMap = fieldProfileList.stream().collect(Collectors.toMap(
                FieldProfile::getId, f -> service.calculateBaseLine(_noticeTime, f.getId())
        ));

        log.info(baseMap.get(fieldId));
        log.info(baseMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));

    }


    @Test
    @Disabled
    void testUpdateQuitBySrIdAndTime() throws ParseException {
        Date time = yyyyMMddHHmmss.parse("20200220 18:25:00");
        log.info(service.updateQuitBySrIdAndTime(1l, time));
    }

    @Test
//     @Disabled
    void testAddSpinReserveData() throws ParseException {

        Date end = yyyyMMddHHmmss.parse("20210705 23:00:00"); //new Date();
//         Date start = DatetimeUtils.add(end, Calendar.MINUTE, -30);
        Date start = yyyyMMddHHmmss.parse("20210704 01:00:00"); //new Date();
        srId = (long) 5;
        //Optional<SpinReserveProfile> opt = service.findSpinReserveProfile(srId);
        List<SpinReserveData> list =
//                 service.findSpinReserveDataBySrIdAndNoticeTime(srId,start,end);
//                 service.findSpinReserveData();
                service.findSpinReserveDataBySrIdAndNoticeTypeAndNoticeTime(srId, NoticeType.UNLOAD, start, end);
        log.info(list.toString());
        log.info("List Size:" + list.size());

//        SpinReserveData srd = new SpinReserveData();
//        SpinReserveProfile spinReserveProfile = new SpinReserveProfile();
//        spinReserveProfile.setId((long)5);
//        Date noticeTime = yyyyMMddHHmmss.parse("20000101 12:00:00");
////        Date noticeTime = DateFormat.getDateTimeInstance().parse("20000101 12:00:00");
//        srd.setNoticeTime(noticeTime);
//        srd.setSpinReserveProfile(spinReserveProfile);
//        srd.setServiceEnergy(new BigDecimal(1000));
//        srd.setPerformanceEnergy(new BigDecimal(2000));
////            log.info("Data info:" + srd.toString());
//        service.saveSpinReserveData(srd);
//        log.info(srd.toString());

//        log.info("Data info:"+srd.toString());
        if (list != null && list.size() > 0) {
            SpinReserveData srd = list.get(0);
            //新增、修改欄位數值
            SpinReserveProfile spinReserveProfile = new SpinReserveProfile();
//            spinReserveProfile.setId((long)9);
            srd.setSpinReserveProfile(spinReserveProfile);
            srd.setServiceEnergy(new BigDecimal(1000));
            srd.setPerformanceEnergy(new BigDecimal(2000));
//            log.info("Data info:" + srd.toString());
            service.saveSpinReserveData(srd);
            log.info(srd.toString());
        }

        // log.info(obj.toString());
//         assertEquals(new BigDecimal(2000),obj.getPowerM1t2());
//         System.out.println("SpinReserveServiceService:"+service);
//         service.saveSpinReserveData(obj);
    }

    @Test
        //Disabled
    void testUpdateSpinReserveData() throws ParseException {

        Date end = yyyyMMddHHmmss.parse("20210705 15:00:00"); //new Date();
        Date start = DatetimeUtils.add(end, Calendar.MINUTE, -30);
//        Date startTest = ;
        srId = (long) 5;
        //Optional<SpinReserveProfile> opt = service.findSpinReserveProfile(srId);
        List<SpinReserveData> list =
                service.findSpinReserveDataBySrIdAndNoticeTypeAndNoticeTime(srId, NoticeType.UNLOAD, start, end);
        log.info(list.toString());
        log.info("List Size:" + list.size());
//        log.info("Data info:"+srd.toString());
        if (list != null && list.size() > 0) {
            SpinReserveData srd = list.get(0);
            //新增、修改欄位數值
//            System.out.println("List Size:"+list.size());
//            System.out.println("Data info:"+srd.toString());
//            log.info("Final List Size:"+list.size());
            log.info("Data info:" + srd.toString());
            service.saveSpinReserveData(srd);
            log.info(srd.toString());
        }
    }

}
