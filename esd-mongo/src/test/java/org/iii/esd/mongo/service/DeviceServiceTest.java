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

import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.mongo.document.DeviceHistory;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.RealTimeData;
import org.iii.esd.mongo.enums.DeviceType;
import org.iii.esd.mongo.enums.LoadType;
import org.iii.esd.mongo.vo.data.measure.MeasureData;
import org.iii.esd.mongo.vo.data.measure.MeterData;
import org.iii.esd.mongo.vo.data.setup.PVSetupData;
import org.iii.esd.utils.DatetimeUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {DeviceService.class, UpdateService.class})
@EnableAutoConfiguration
@Log4j2
class DeviceServiceTest extends AbstractServiceTest {

    @Value("${deviceId}")
    private String deviceId;

    @Value("${fieldId}")
    private Long fieldId;

    @Autowired
    private DeviceService service;

    @Value("#{'${test.deviceHistoryTime}'.split(',')}")
    private List<String> deviceHistoryTime;

    @Test
    @Disabled
    void testAdd() {
        DeviceType dt = DeviceType.Meter;
        DeviceProfile obj = new DeviceProfile();
        String id = "II".concat(dt.getCode()).concat("DTI---" + getRamdom(10, 0));
        //String id = "II10ABCDEF0123456789";
        log.info(id);
        obj.setName("III-PV");
        obj.setId(id);
        obj.setFieldProfile(new FieldProfile(fieldId));
        obj.setDeviceType(dt);
        obj.setLoadType(LoadType.M2);
        //obj.setParent(new DeviceProfile(id));
        obj.setConnectionStatus(ConnectionStatus.Connected);
        obj.setISetupData(PVSetupData.builder().
                unitCost(2).
                                             pvCapacity(3).
                                             maintenanceCost(new BigDecimal("1")).
                                             build());
        service.add(obj);
        RealTimeData realTimeData = new RealTimeData(id, obj, new Date(), MeterData.builder().
                activePower(new BigDecimal(getRamdom(2, 3))).
                                                                                           kWh(new BigDecimal(getRamdom(2, 3))).
                                                                                           build().wrap());
        service.saveRealTimeData(realTimeData);

        //		DeviceType dt = DeviceType.Battery;
        //		DeviceProfile obj = new DeviceProfile();
        //		String id = "II".concat(dt.getCode()).concat("DTI---"+getRamdom(10,0));
        //		obj.setName("亞力測試電池");
        //		obj.setId(id);
        //		obj.setFieldProfile(new FieldProfile(999l));
        //		obj.setDeviceType(dt);
        //		obj.setLoadType(LoadType.M3);
        //		obj.setConnectionStatus(ConnectionStatus.Connected);
        //		obj.setISetupData(BatterySetupData.builder().
        //				capacity(new BigDecimal("10")).
        //				chargeKw(new BigDecimal("10")).
        //				dischargeKw(new BigDecimal("10")).
        //				chargeEfficiency(new BigDecimal("96.4")).
        //				dischargeEfficiency(new BigDecimal("96.4")).
        //				dod(new BigDecimal("83.1")).
        //				socMax(100).
        //				socMin(20).
        //				selfDischargeKw(new BigDecimal("0")).
        //				lifecycle(3000).
        //				constructionCost(100000).
        //				capacityCost(26894).
        //				kWcost(4375).
        //				maintenanceCost(new BigDecimal("2.34")).
        //			build());
        //		service.add(obj);
        //
        //		RealTimeData realTimeData = new RealTimeData(id, obj, new Date(),
        //				MeterData.builder().
        //					activePower(new BigDecimal(getRamdom(2,3))).
        //					kWh(new BigDecimal(getRamdom(2,3))).
        //				build().wrap());
        //		service.saveRealTimeData(realTimeData);
    }

    @Test
    @Disabled
    void testUpdateRealTimeData() {
        Optional<RealTimeData> opt = service.findRealTimeDataById(deviceId);
        if (opt.isPresent()) {
            RealTimeData obj = opt.get();
            obj.setReportTime(new Date());
            obj.setMeasureData(MeterData.builder().
                    activePower(new BigDecimal(getRamdom(2, 3))).
                                                kWh(new BigDecimal(getRamdom(2, 3))).
                                                build());
            log.info(service.saveRealTimeData(obj).toString());
        } else {
            //    		RealTimeData obj = new RealTimeData(deviceId);
            //    		DeviceProfile dp = new DeviceProfile(deviceId);
            //    		dp.setDeviceType(DeviceType.getCode(deviceId.substring(2, 4)));
            //    		obj.setDeviceId(dp);
            //    		obj.setReportTime(new Date());
            //    		obj.setMeasureData(MeterData.builder().
            //    				activePower(new BigDecimal(getRamdom(2,3))).
            //    				kWh(new BigDecimal(getRamdom(2,3))).
            //    			build());
            //    		log.info(service.saveRealTimeData(obj).toString());

            fail();
        }
    }

    @Test
    @Disabled
    void testUpdate() {
        Optional<DeviceProfile> opt = service.findDeviceProfileById(deviceId);
        if (opt.isPresent()) {
            DeviceProfile obj = opt.get();
            obj.setName("亞力測試電池");
            log.info(service.saveDeviceProfile(obj).toString());
        } else {
            fail();
        }
    }

    @Test
    @Disabled
    void testUpdateConnectionStatusById() {
        assertTrue(service.updateConnectionStatusAndReportTimeById(new Date(), deviceId));
    }

    @Test
    @Disabled
    void testUpdateIsSyncById() {
        assertTrue(service.updateIsSyncById(false, deviceId));
    }

    @Test
    @Disabled
    void testUpdateIsSyncByFieldId() {
        assertTrue(service.updateIsSyncByFieldId(true, fieldId));
    }

    @Test
    void testFind() {
        Optional<DeviceProfile> opt = service.findDeviceProfileById(deviceId);
        if (opt.isPresent()) {
            log.info(opt.get().toString());
        } else {
            fail();
        }
    }

    @Test
    void testFindByIdSet() {
        Set<String> idSet = new HashSet<>(Arrays.asList("123", "456"));
        List<RealTimeData> list = service.findRealTimeDataByDeviceIdSet(idSet);
        for (RealTimeData realTimeData : list) {
            log.info(realTimeData.toString());
        }
    }

    @Test
    void testFindByFieldId() {
        List<DeviceProfile> list = service.findDeviceProfileByFieldId(fieldId);
        for (DeviceProfile deviceProfile : list) {
            log.info(deviceProfile.toString());
        }
    }

    @Test
    void testFindByFieldIdAndLoadType() {
        List<DeviceProfile> list = service.findDeviceProfileByFieldIdAndLoadType(fieldId, LoadType.M2);
        for (DeviceProfile deviceProfile : list) {
            log.info(deviceProfile.toString());
        }
    }

    @Test
    void testFindByParentId() {
        List<DeviceProfile> list = service.findDeviceProfileByParentId(deviceId);
        for (DeviceProfile deviceProfile : list) {
            log.info(deviceProfile.toString());
        }
    }

    @Test
    void testCountByFieldId() {
        int count = service.countByFieldId(fieldId);
        log.info(count);
        assertTrue(count > 0);
    }

    @Test
    void testFindRealTimeDataByFieldId() throws JsonProcessingException {
        List<RealTimeData> list = service.findRealTimeDataByFieldId(fieldId);
        for (RealTimeData realTimeData : list) {
            ObjectMapper mapper = new ObjectMapper();
            log.info(mapper.writeValueAsString(realTimeData));
            log.info(realTimeData.getDeviceId());
        }
    }

    @Test
    void testFindDisconnectRealTimeData() throws JsonProcessingException {
        List<RealTimeData> list = service.findDisconnectRealTimeData(3);

        List<DeviceProfile> DeviceProfileList = list.stream().map(rt -> rt.getDeviceId()).collect(Collectors.toList());

        for (DeviceProfile deviceProfile : DeviceProfileList) {
            ObjectMapper mapper = new ObjectMapper();
            log.info(mapper.writeValueAsString(deviceProfile));
        }

        //		for (RealTimeData realTimeData : list) {
        //	        ObjectMapper mapper = new ObjectMapper();
        //	        log.info(mapper.writeValueAsString(realTimeData));
        //			log.info(realTimeData.getDeviceId());
        //		}
    }


    @Test
    void testFindDeviceHistoryByDeviceIdAndTime() throws ParseException {
        //		Date start = yyyyMMddHHmmss.parse(deviceHistoryTime.get(0));
        //		Date end = yyyyMMddHHmmss.parse(deviceHistoryTime.get(1));

        Date start = DatetimeUtils.add(new Date(), Calendar.MINUTE, -10);
        System.out.println(start);
        Date end = DatetimeUtils.add(new Date(), Calendar.MINUTE, 0);
        System.out.println(end);
        List<DeviceHistory> list = service.findDeviceHistoryByDeviceIdAndTime(deviceId, start, end);
        for (DeviceHistory deviceHistory : list) {
            log.info(deviceHistory.toString());
        }
    }

    @Test
    void testFindLastDeviceHistoryByDeviceIdAndTime() throws ParseException {
        Date time = DatetimeUtils.add(new Date(), Calendar.MINUTE, -10);
        System.out.println(time);
        DeviceHistory deviceHistory = service.findLastDeviceHistoryByDeviceIdAndTime(deviceId, time);

        if (deviceHistory != null) {
            log.info(deviceHistory.getReportTime());
        }

    }


    @Test
    @Disabled
    void testAddDeviceHistory() throws ParseException {
        Date reportTime = yyyyMMddHHmmss.parse("20201204 12:50:00");
        MeasureData measureData = MeasureData.builder().
                activePower(new BigDecimal("8.765")).
                                                     kWh(new BigDecimal("271418510")).
                                                     powerFactor(new BigDecimal("0.9792")).
                                                     kVAR(new BigDecimal("0.69")).
                                                     voltageA(new BigDecimal("0")).
                                                     currentA(new BigDecimal("22.53")).
                                                     voltageB(new BigDecimal("0")).
                                                     currentB(new BigDecimal("0")).
                                                     voltageC(new BigDecimal("0")).
                                                     currentC(new BigDecimal("22.6175")).
                                                     build();

        RealTimeData realTimeData = new RealTimeData(deviceId, new DeviceProfile(deviceId), reportTime, measureData);
        DeviceHistory deviceHistory = new DeviceHistory(new DeviceProfile(deviceId), reportTime, measureData);
        service.saveRealTimeData(realTimeData);
        service.saveDeviceHistory(deviceHistory);
    }

    @Test
    @Disabled
    void testDelete() {
        DeviceType dt = DeviceType.Meter;
        DeviceProfile obj = new DeviceProfile();
        String id = "II".concat(dt.getCode()).concat("DTI---" + getRamdom(10, 0));
        log.info(id);
        obj.setName("III電表3");
        obj.setId(id);
        service.add(obj);
        service.delete(id);
        Optional<DeviceProfile> opt = service.findDeviceProfileById(id);
        if (opt.isPresent()) {
            fail("delete fail.");
        }
    }

    //	@Test
    //	void testAddBattery() {
    //		DeviceType dt = DeviceType.Battery;
    //		DeviceProfile obj = new DeviceProfile();
    //		obj.setName("亞力測試電池");
    //		obj.setDeviceId("II".concat(dt.getCode()).concat("DTI---"+getRamdom(10,0)));
    //		obj.setFieldProfile(new FieldProfile(2l));
    //		obj.setDeviceType(dt);
    //		obj.setLoadType(LoadType.M3);
    //		obj.setConnectionStatus(ConnectionStatus.Connected);
    //		obj.setSetupData(BatterySetupData.builder().
    //				capacity(new BigDecimal("10")).
    //				chargeKw(new BigDecimal("10")).
    //				dischargeKw(new BigDecimal("10")).
    //				chargeEfficiency(new BigDecimal("96.4")).
    //				dischargeEfficiency(new BigDecimal("96.4")).
    //				dod(new BigDecimal("83.1")).
    //				socMax(100).
    //				socMin(20).
    //				selfDischargeKw(new BigDecimal("0")).
    //				lifecycle(3000).
    //				constructionCost(100000).
    //				capacityCost(26894).
    //				kWcost(4375).
    //				maintenanceCost(new BigDecimal("2.34")).
    //			build());
    //		long seq = service.add(obj);
    //		log.info(seq);
    //
    //	}
    //
    //	@Test
    //	void testAddPv() {
    //		DeviceType dt = DeviceType.Meter;
    //		DeviceProfile obj = new DeviceProfile();
    //		obj.setName("亞力測試PV");
    //		obj.setDeviceId("II".concat(dt.getCode()).concat("DTI---"+getRamdom(10,0)));
    //		obj.setFieldProfile(new FieldProfile(2l));
    //		obj.setDeviceType(dt);
    //		obj.setLoadType(LoadType.M2);
    //		obj.setConnectionStatus(ConnectionStatus.Connected);
    //		obj.setSetupData(PVSetupData.builder().
    //				pvCapacity(156).
    //				unitCost(51600).
    //				maintenanceCost(new BigDecimal("2.34")).
    //			build());
    //		long seq = service.add(obj);
    //		log.info(seq);
    //
    //	}
    //
    //	@Test
    //	void testAddController() {
    //		DeviceType dt = DeviceType.Meter;
    //		DeviceProfile obj = new DeviceProfile();
    //		obj.setName("測試冰機BD_Admin_1");
    //		obj.setDeviceId("II".concat(dt.getCode()).concat("DTI---"+getRamdom(10,0)));
    //		obj.setFieldProfile(new FieldProfile(2l));
    //		obj.setDeviceType(dt);
    //		obj.setLoadType(LoadType.M6);
    //		obj.setConnectionStatus(ConnectionStatus.Connected);
    //		obj.setSetupData(ControllerSetupData.builder().
    //				fullCapacity(new BigDecimal("140")).
    //				unloadCapacity(new BigDecimal("140")).
    //				unloadTime(0).
    //				returnTime(0).
    //				cost(new BigDecimal(0)).
    //			build());
    //		long seq = service.add(obj);
    //		log.info(seq);
    //
    //	}

}