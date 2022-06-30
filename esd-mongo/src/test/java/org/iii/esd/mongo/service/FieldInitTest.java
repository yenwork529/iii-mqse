package org.iii.esd.mongo.service;

import java.math.BigDecimal;
import java.util.Date;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.enums.TouType;
import org.iii.esd.mongo.document.SiloCompanyProfile;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.PolicyProfile;
import org.iii.esd.mongo.document.RealTimeData;
import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.mongo.enums.DeviceType;
import org.iii.esd.mongo.enums.LoadType;
import org.iii.esd.mongo.vo.data.measure.MeterData;
import org.iii.esd.mongo.vo.data.setup.SetupData;

@SpringBootTest(classes = {
        SiloCompanyProfileService.class,
        FieldProfileService.class,
        SpinReserveService.class,
        QueryService.class,
        StatisticsService.class,
        DeviceService.class,
        UpdateService.class,
})
@EnableAutoConfiguration
@Log4j2
public class FieldInitTest extends AbstractServiceTest {

    private static final int TEST_QSE_CODE = 300;
    private static final int TEST_TXG_CODE = 310;
    private static final int TEST_SERVICE_TYPE = 3;
    private static final String TEST_DNP_URL = "http://127.0.0.1:8585";  // for docker
    private static final String TEST_CALLBACK_URL = "http://127.0.0.1:8585";  // for docker
    // private static final String TEST_DNP_URL = "http://140.92.24.20:8585";
    // private static final String TEST_CALLBACK_URL = "http://140.92.24.20:8585";
    private static final int TEST_RES_1 = 311;
    private static final int TEST_RES_2 = 312;
    private static final String TEST_DEVICE_1 = "II10MAYA--------M1";
    private static final String TEST_DEVICE_2 = "II10MAYA--------M2";
    // private static final String TEST_TC_IP = "esd-client";  // for docker
    private static final String TEST_TC_IP = "127.0.0.1";  // for local
    private static final String TEST_NAME = "馬雅測試";
    private static final long TEST_CT = 1L;
    // private static final long TEST_CT = 60L;
    private static final long TEST_PT = 1L;
    // private static final long TEST_PT = 200L;

    @Autowired
    private SiloCompanyProfileService siloCompanyProfileService;

    @Autowired
    private SpinReserveService spinReserveService;

    @Autowired
    private FieldProfileService fieldProfileService;

    @Autowired
    private DeviceService deviceService;

    @Test
    public void testAddCaseToServer() {
        SiloCompanyProfile company = addCompany();
        SpinReserveProfile sr = addSpinReserve(company);
        FieldProfile field1 = addField(company, sr, 0, TEST_RES_1);
        FieldProfile field2 = addField(company, sr, 1, TEST_RES_2);
        addDevice(field1, TEST_DEVICE_1);
        addDevice(field2, TEST_DEVICE_2);
    }

    @Test
    public void testAddCaseToTC() {
        SiloCompanyProfile company = addCompany();
        SpinReserveProfile sr = addSpinReserve(company);
        FieldProfile field1 = addField(company, sr, 0, TEST_RES_1);
        FieldProfile field2 = addField(company, sr, 1, TEST_RES_2);
        addDevice(field1, TEST_DEVICE_1);
        addDevice(field2, TEST_DEVICE_2);
    }

    private void addDevice(FieldProfile field, String id) {
        DeviceType dt = DeviceType.Meter;
        DeviceProfile device = new DeviceProfile();
        log.info(id);

        device.setName(TEST_NAME + "-電錶-" + field.getName());
        device.setId(id);
        device.setFieldProfile(field);
        device.setDeviceType(dt);
        device.setLoadType(LoadType.M1);
        device.setConnectionStatus(ConnectionStatus.Connected);
        device.setMainLoad(true);
        device.setEnableStatus(EnableStatus.enable);
        device.setSetupData(SetupData.builder()
                                     .ct(BigDecimal.valueOf(TEST_CT))
                                     .pt(BigDecimal.valueOf(TEST_PT))
                                     .build());

        deviceService.add(device);
        RealTimeData realTimeData = new RealTimeData(id, device, new Date(),
                MeterData.builder()
                         .activePower(new BigDecimal(getRamdom(2, 3)))
                         .kWh(new BigDecimal(getRamdom(2, 3)))
                         .build()
                         .wrap());
        deviceService.saveRealTimeData(realTimeData);
    }

    private FieldProfile addField(SiloCompanyProfile company, SpinReserveProfile sr, int srIndex, int resCode) {
        FieldProfile field = new FieldProfile();
        field.setName(TEST_NAME + "-場域-" + resCode);
        field.setTouType(TouType.TPH3S);
        field.setSiloCompanyProfile(company);
        field.setSpinReserveProfile(sr);
        field.setSrIndex(srIndex);
        field.setTcEnable(EnableStatus.enable);
        field.setTcIp(TEST_TC_IP);
        field.setResCode(resCode);
        field.setPolicyProfile(new PolicyProfile(1L));
        field.setTyod(0);
        field.setTyodc(0);
        field.setTrhd(0);
        field.setOyod(0);
        field.setIsReserve(true);
        field.setTargetType(1);
        field.setDevStatus(ConnectionStatus.Connected);
        field.setUnload(0);
        field.setIsSync(true);
        field.setIsNeedReschedule(false);
        field.setDelay(0);

        long seq = fieldProfileService.add(field);
        log.info(seq);

        return field;
    }

    private SpinReserveProfile addSpinReserve(SiloCompanyProfile company) {
        SpinReserveProfile sr = new SpinReserveProfile();
        sr.setName(TEST_NAME + "-SR");
        sr.setSiloCompanyProfile(company);
        sr.setEnableStatus(EnableStatus.enable);
        sr.setDnpURL(TEST_DNP_URL);
        sr.setBidContractCapacity(3600);
        sr.setClipKW(4800);

        long seq = spinReserveService.addSpinReserveProfile(sr);
        log.info(seq);

        return sr;
    }

    private SiloCompanyProfile addCompany() {
        SiloCompanyProfile company = new SiloCompanyProfile();
        company.setName(TEST_NAME);
        company.setQseCode(TEST_QSE_CODE);
        company.setTgCode(TEST_TXG_CODE);
        company.setServiceType(TEST_SERVICE_TYPE);
        company.setDnpURL(TEST_DNP_URL);
        company.setCallbackURL(TEST_CALLBACK_URL);

        long seq = siloCompanyProfileService.add(company);
        log.info(seq);

        return company;
    }

}
