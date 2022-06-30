package org.iii.esd.mongo.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.Constants;
import org.iii.esd.enums.DataType;
import org.iii.esd.enums.StatisticsType;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.DeviceStatistics;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.vo.aggregate.AelectricData;
import org.iii.esd.utils.DatetimeUtils;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {StatisticsService.class, QueryService.class})
@EnableAutoConfiguration
@Log4j2
class StatisticsServiceTest extends AbstractServiceTest {

    @Value("${deviceId}")
    private String deviceId;

    @Value("${fieldId}")
    private Long fieldId;

    @Value("${srId}")
    private Long srId;

    private StatisticsType statisticsType = StatisticsType.min15;

    private DataType dataType = DataType.T1;

    @Value("#{'${test.deviceStatisticsTime}'.split(',')}")
    private List<String> deviceStatisticsTime;

    @Value("#{'${test.electricDataTime}'.split(',')}")
    private List<String> electricDataTime;

    @Value("#{'${test.spinReserveDataTime}'.split(',')}")
    private List<String> spinReserveDataTime;

    @Autowired
    private StatisticsService service;

    @Test
    void testFindDeviceStatisticsByDeviceIdAndTime() throws ParseException {
        Date start = yyyyMMddHHmmss.parse(deviceStatisticsTime.get(0));
        Date end = yyyyMMddHHmmss.parse(deviceStatisticsTime.get(1));
        List<DeviceStatistics> list = service.findDeviceStatisticsByDeviceIdAndTime(deviceId, start, end);
        for (DeviceStatistics deviceStatistics : list) {
            log.info(deviceStatistics.toString());
        }
    }

    @Test
    void testFindDeviceStatisticsByDeviceIdAndStatisticsTypeAndTime() throws ParseException {
        Date start = yyyyMMddHHmmss.parse(deviceStatisticsTime.get(0));
        Date end = yyyyMMddHHmmss.parse(deviceStatisticsTime.get(1));
        List<DeviceStatistics> list = service.findDeviceStatisticsByDeviceIdAndStatisticsTypeAndTime(deviceId, statisticsType, start, end);
        for (DeviceStatistics deviceStatistics : list) {
            log.info(deviceStatistics.toString());
        }
    }

    @Test
    @Disabled
    void testAddDeviceStatistics() throws ParseException {
        Date start = yyyyMMddHHmmss.parse(deviceStatisticsTime.get(0));
        Date end = yyyyMMddHHmmss.parse(deviceStatisticsTime.get(1));
        System.out.println(deviceId);
        service.delete(deviceId, statisticsType, start, end);

        while (start.getTime() <= end.getTime()) {
            BigDecimal kWh = new BigDecimal(getRamdom(2, 3));
            DeviceStatistics ds = DeviceStatistics.builder().
                    deviceProfile(new DeviceProfile(deviceId)).
                                                          statisticsType(statisticsType).
                                                          time(start).
                                                          msoc(kWh).
                                                          build();
            service.saveDeviceStatistics(ds);
            start = new Date(start.getTime() + 15 * 60 * 1000);
        }
    }

    @Test
    @Disabled
    void testDeleteDeviceStatistics() throws ParseException {
        Date start = yyyyMMddHHmmss.parse(deviceStatisticsTime.get(0));
        Date end = yyyyMMddHHmmss.parse(deviceStatisticsTime.get(1));
        service.delete(deviceId, statisticsType, start, end);
    }

    @Test
    void testFindElectricDataByFieldIdAndTime() throws ParseException {
        Date start = yyyyMMddHHmmss.parse(electricDataTime.get(0));
        Date end = yyyyMMddHHmmss.parse(electricDataTime.get(1));
        List<ElectricData> list = service.findElectricDataByFieldIdAndTime(fieldId, start, end);
        for (ElectricData electricData : list) {
            log.info(electricData.toString());
        }
    }

    @Test
    void testFindLastElectricDataByFieldIdAndDataType() {

        Optional<ElectricData> opt = service.findLastElectricDataByFieldIdAndDataType(fieldId, dataType);
        if (opt.isPresent()) {
            ElectricData obj = opt.get();
            log.info(obj.toString());
        } else {
            fail();
        }
    }

    @Test
    void testFindElectricDataByFieldIdAndDeviceTypeAndTime() throws ParseException, JsonProcessingException {
        Date start = yyyyMMddHHmmss.parse(electricDataTime.get(0));
        Date end = yyyyMMddHHmmss.parse(electricDataTime.get(1));

        List<ElectricData> list = service.findElectricDataByFieldIdAndDataTypeAndTime(fieldId, dataType, start, end);
        log.info(list.size());
        for (ElectricData electricData : list) {
            log.info(electricData.toString());
            ObjectMapper mapper = new ObjectMapper();
            log.info(mapper.writeValueAsString(electricData));
        }
    }

    @Test
    void testFindElectricDataByFieldIdInAndDeviceTypeAndTime() throws ParseException, JsonProcessingException {
        Date start = yyyyMMddHHmmss.parse(electricDataTime.get(0));
        Date end = yyyyMMddHHmmss.parse(electricDataTime.get(1));

        Set<Long> idSet = new HashSet<>(Arrays.asList(1l, 2l));
        List<ElectricData> list = service.findElectricDataByFieldIdAndDataTypeAndTime(idSet, dataType, start, end);
        log.info(list.size());
        for (ElectricData electricData : list) {
            log.info(electricData.toString());
            ObjectMapper mapper = new ObjectMapper();
            log.info(mapper.writeValueAsString(electricData));
        }
    }

    @Test
    void testFindByFieldIdAndDataTypeAndTimeGreaterThanEqual() throws ParseException, JsonProcessingException {
        Set<Long> idSet = new HashSet<>(Arrays.asList(1l, 2l));
        List<ElectricData> list = service.findByFieldIdAndDataTypeAndTimeGreaterThanEqual(idSet, DataType.T99,
                DatetimeUtils.add(new Date(), Calendar.MINUTE, 5));
        log.info(list.size());
        for (ElectricData electricData : list) {
            log.info(electricData.toString());
            ObjectMapper mapper = new ObjectMapper();
            log.info(mapper.writeValueAsString(electricData));
        }
    }

    @Test
    void testFindByFieldIdAndTimeAndNeedFix() throws ParseException, JsonProcessingException {
        Set<Long> idSet = new HashSet<>(Arrays.asList(1l, 2l, 3l, 4l));
        Date end = DatetimeUtils.getFirstHourOfDay(new Date());
        Date start = DatetimeUtils.add(end, Calendar.DATE, -2);
        List<ElectricData> list = service.findByFieldIdAndTimeAndNeedFix(idSet, start, end);
        log.info(list.size());
        for (ElectricData electricData : list) {
            log.info("{} - {}", electricData.getFieldProfile().getName(), Constants.TIMESTAMP_FORMAT.format(electricData.getTime()));
            ObjectMapper mapper = new ObjectMapper();
            log.info(mapper.writeValueAsString(electricData));
        }
    }

    @Test
    void testAggregateElectricDataByIdsAndTime() throws ParseException, JsonProcessingException {
        Set<Long> idSet = new HashSet<>(Arrays.asList(1l, 2l, 3l, 4l));
        Date end = DatetimeUtils.getFirstHourOfDay(new Date());
        Date start = DatetimeUtils.add(end, Calendar.DATE, -1);

        List<AelectricData> list = service.aggregateElectricDataByIdsAndTime(idSet, start, end);
        log.info(list.size());
        for (AelectricData aelectricData : list) {
            //log.info(aelectricData.toString());
            //log.info(aelectricData.get_id());
            ObjectMapper mapper = new ObjectMapper();
            log.info(mapper.writeValueAsString(aelectricData));
        }
    }


    @Test
    @Disabled
    void testAddElectricData() throws ParseException {
        Date start = yyyyMMddHHmmss.parse(electricDataTime.get(0));
        Date end = yyyyMMddHHmmss.parse(electricDataTime.get(1));
        service.delete(1l, dataType, start, end);

        while (start.getTime() <= end.getTime()) {
            service.saveElectricData(genElectricData(1l, dataType, start));
            start = new Date(start.getTime() + 15 * 60 * 1000);
        }
    }

    @Test
    @Disabled
    void testDeleteElectricData() throws ParseException {
        Date start = yyyyMMddHHmmss.parse(electricDataTime.get(0));
        Date end = yyyyMMddHHmmss.parse(electricDataTime.get(1));
        service.delete(fieldId, dataType, start, end);
    }

    @Test
    @Disabled
    void testAdd() throws ParseException {
        List<ElectricData> list = Arrays.asList(
                genElectricData(11l, dataType, yyyyMMddHHmmss.parse("20200219 10:30:00")),
                genElectricData(11l, dataType, yyyyMMddHHmmss.parse("20200219 11:00:00")),
                genElectricData(11l, dataType, yyyyMMddHHmmss.parse("20200219 11:30:00")),
                genElectricData(11l, dataType, yyyyMMddHHmmss.parse("20200219 12:00:00")),
                genElectricData(12l, dataType, yyyyMMddHHmmss.parse("20200219 10:30:00")),
                genElectricData(12l, dataType, yyyyMMddHHmmss.parse("20200219 11:00:00")),
                genElectricData(12l, dataType, yyyyMMddHHmmss.parse("20200219 11:30:00")),
                genElectricData(12l, dataType, yyyyMMddHHmmss.parse("20200219 12:00:00"))
        );
        service.saveElectricData(list);
    }

    @Test
    void testFindSpinReserveHistoryData() throws ParseException, JsonProcessingException {
        Date start = yyyyMMddHHmmss.parse("20200219 16:43:00");
        Date end = yyyyMMddHHmmss.parse("20200219 16:48:00");
        Set<Long> idSet = new HashSet<>(Arrays.asList(1l, 3l));

        List<ElectricData> edList = service.findElectricDataByFieldIdAndDataTypeAndTime(
                idSet,
                DataType.T99, start, end);

        for (ElectricData electricData : edList) {
            //log.info(electricData.toString());
            ObjectMapper mapper = new ObjectMapper();
            log.info(mapper.writeValueAsString(electricData));
        }

        Double baseLine = edList.stream()
                                .collect(Collectors.groupingBy(ElectricData::getTime,
                                        Collectors.reducing(new ElectricData(), ElectricData::sum)))
                                .values().stream().map(ed -> ed.getM1kW())
                                .collect(Collectors.averagingDouble(d -> d.doubleValue()));

        log.info(baseLine);
        //		List<ElectricData> list = service.findElectricDataByFieldIdAndDataTypeAndTime(idSet, dataType, start, end);
        //
        //		Map<Date, ElectricData> map = list.stream().
        //			collect(Collectors.groupingBy(
        //				ElectricData::getTime, Collectors.reducing(new ElectricData(), ElectricData::sum)));
        //
        //		List<ElectricData> list2 = map.entrySet().stream().
        //			sorted((d1, d2) -> d1.getKey().compareTo(d2.getKey())).
        //			map(s-> s.getValue()).collect(Collectors.toList());
        //
        //		ElectricData ed = ElectricData.builder().
        //				time(yyyyMMddHHmmss.parse("20200219 11:02:00")).
        //				m0kW(new BigDecimal("10000")).
        //				build();
        //
        //		if(list2.stream().filter(e->e.getTime().equals(ed.getTime())).count()==0) {
        //			list2.add(ed);
        //			list2=list2.stream().
        //					sorted(Comparator.comparing(ElectricData::getTime)).
        //					collect(Collectors.toList());
        //		}else {
        //			list2.replaceAll(e-> e.getTime().equals(ed.getTime())?e.sum(ed):e);
        //		}

        //		for (ElectricData electricData : list2) {
        //			log.info(electricData.toString());
        //	        ObjectMapper mapper = new ObjectMapper();
        //	        log.info(mapper.writeValueAsString(electricData));
        //		}
    }

    private ElectricData genElectricData(Long fieldId, DataType dataType, Date date) {
        BigDecimal m1 = new BigDecimal(getRamdom(3, 3));
        BigDecimal m2 = new BigDecimal(getRamdom(2, 3));
        BigDecimal m3 = new BigDecimal(getRamdom(2, 3));
        BigDecimal m6 = new BigDecimal(getRamdom(2, 3));
        BigDecimal m7 = new BigDecimal(getRamdom(2, 3));

        ElectricData ed = ElectricData.builder().

                                      fieldProfile(new FieldProfile(fieldId)).
                                      dataType(dataType).
                                      time(date).
                                      m1kW(m1).
                                      m2kW(m2).
                                      m3kW(m3).
                                      m6kW(m6).
                                      m7kW(m7).
                                      build();
        ed.init();
        return ed;
    }

}