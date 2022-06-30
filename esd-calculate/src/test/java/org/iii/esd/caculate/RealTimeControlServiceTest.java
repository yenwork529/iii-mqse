package org.iii.esd.caculate;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.control.RealTimeControlInputModel;
import org.iii.esd.control.RealTimeControlService;
import org.iii.esd.control.RecommendControl;
import org.iii.esd.enums.DataType;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.PolicyProfile;
import org.iii.esd.mongo.repository.ElectricDataRepository;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.mongo.service.UpdateService;

@SpringBootTest(classes = {RealTimeControlService.class, FieldProfileService.class, UpdateService.class})
@EnableAutoConfiguration
@Log4j2
class RealTimeControlServiceTest extends AbstractServiceTest {

    long fieldId = 999L;
    @Autowired
    ElectricDataRepository electricDataRepository;
    @SuppressWarnings("deprecation")
    Date historyStartDate = new Date("2017/07/12");
    @SuppressWarnings("deprecation")
    Date Control_Quarter_Start = new Date("2017/07/12 15:00");
    @SuppressWarnings("deprecation")
    Date Control_Start = new Date("2017/07/12 15:09");
    @Autowired
    private RealTimeControlService service;
    private BigDecimal batterykWH = BigDecimal.valueOf(50);
    private BigDecimal chargeEffi = BigDecimal.valueOf(0.9);
    private BigDecimal maxPower = BigDecimal.valueOf(25);
    private BigDecimal dischargeEffi = BigDecimal.valueOf(0.9);
    private BigDecimal TYOD = BigDecimal.valueOf(200);
    private int historyDataType = DataType.T1.getCode();
    private int realTimeDataType = DataType.T99.getCode();
    private int scheduleDataType = DataType.T11.getCode();

    FieldProfile GetField(long fieldId) {
        FieldProfile field = new FieldProfile();
        field.setId(fieldId);
        return field;
    }

    @Test
    @Order(1)
    void Minutes3Test() throws Exception {

        RealTimeControlInputModel model = new RealTimeControlInputModel();
        model.Battery_Capacity = batterykWH;
        model.Charge_Efficiency = chargeEffi;
        model.config = PolicyProfile.Default();
        model.Control_Start = Control_Start;
        model.Discharge_Efficiency = dischargeEffi;
        model.fieldId = fieldId;
        model.Max_Power = maxPower;
        model.REAL_DATA_TYPE = historyDataType;
        model.REAL_TIME_DATA_TYPE = realTimeDataType;
        model.SCHEDULE_DATA_TYPE = scheduleDataType;
        model.timeInterval = 3 * 60;
        model.TYOD = TYOD;
        try {
            Calendar dataStart = Calendar.getInstance();
            dataStart.setTime(Control_Quarter_Start);
            Calendar dataEnd = Calendar.getInstance();
            dataEnd.setTime(Control_Start);
            while (dataStart.compareTo(dataEnd) <= 0) {
                List<ElectricData> datas = electricDataRepository.findByFieldIdAndDataTypeAndTime(fieldId, DataType.T99,
                        dataStart.getTime());
                if (datas.size() == 0) {
                    ElectricData data = new ElectricData();
                    data.setDataType(DataType.T99);
                    data.setFieldProfile(GetField(fieldId));
                    data.setTime(dataStart.getTime());
                    data.setM5kW(BigDecimal.valueOf(Math.random() * 100 * 5 + 300));
                    data.setM2kW(BigDecimal.valueOf(Math.random() * 100 * 3 + 00));
                    data.setMsoc(BigDecimal.valueOf(50));
                    data.balance();
                    electricDataRepository.insert(data);
                }
                dataStart.add(Calendar.SECOND, model.timeInterval);
            }
        } catch (Throwable ex) {
            log.error(ex.getMessage(), ex);
        }
        RecommendControl result = service.MainControl(model);
        log.trace(result);
        // Assert.assertEquals(expected, actual);
    }

    @Test
    @Order(2)
    void Minutes1Test() throws Exception {
        RealTimeControlInputModel model = new RealTimeControlInputModel();
        model.Battery_Capacity = batterykWH;
        model.Charge_Efficiency = chargeEffi;
        model.config = PolicyProfile.Default();
        model.Control_Start = Control_Start;
        model.Discharge_Efficiency = dischargeEffi;
        model.fieldId = fieldId;
        model.Max_Power = maxPower;
        model.REAL_DATA_TYPE = historyDataType;
        model.REAL_TIME_DATA_TYPE = realTimeDataType;
        model.SCHEDULE_DATA_TYPE = scheduleDataType;
        model.timeInterval = 1 * 60;
        model.TYOD = TYOD;
        try {
            Calendar dataStart = Calendar.getInstance();
            dataStart.setTime(Control_Quarter_Start);
            Calendar dataEnd = Calendar.getInstance();
            dataEnd.setTime(Control_Start);
            while (dataStart.compareTo(dataEnd) <= 0) {
                List<ElectricData> datas = electricDataRepository.findByFieldIdAndDataTypeAndTime(fieldId, DataType.T99,
                        dataStart.getTime());
                if (datas.size() == 0) {
                    ElectricData data = new ElectricData();
                    data.setDataType(DataType.T99);
                    data.setFieldProfile(GetField(fieldId));
                    data.setTime(dataStart.getTime());
                    data.setM5kW(BigDecimal.valueOf(Math.random() * 100 * 5 + 300));
                    data.setM2kW(BigDecimal.valueOf(Math.random() * 100 * 3 + 00));
                    data.setMsoc(BigDecimal.valueOf(50));
                    data.balance();
                    electricDataRepository.insert(data);
                }
                dataStart.add(Calendar.SECOND, model.timeInterval);
            }
        } catch (Throwable ex) {
            log.error(ex.getMessage(), ex);
        }
        RecommendControl result = service.MainControl(model);
        log.trace(result);
        // Assert.assertEquals(expected, actual);
    }

    @Test
    @Order(3)
    void Seconds30Test() throws Exception {
        RealTimeControlInputModel model = new RealTimeControlInputModel();
        model.Battery_Capacity = batterykWH;
        model.Charge_Efficiency = chargeEffi;
        model.config = PolicyProfile.Default();
        model.Control_Start = Control_Start;
        model.Discharge_Efficiency = dischargeEffi;
        model.fieldId = fieldId;
        model.Max_Power = maxPower;
        model.REAL_DATA_TYPE = historyDataType;
        model.REAL_TIME_DATA_TYPE = realTimeDataType;
        model.SCHEDULE_DATA_TYPE = scheduleDataType;
        model.timeInterval = 30;
        model.TYOD = TYOD;
        try {
            Calendar dataStart = Calendar.getInstance();
            dataStart.setTime(Control_Quarter_Start);
            Calendar dataEnd = Calendar.getInstance();
            dataEnd.setTime(Control_Start);
            while (dataStart.compareTo(dataEnd) <= 0) {
                List<ElectricData> datas = electricDataRepository.findByFieldIdAndDataTypeAndTime(fieldId, DataType.T99,
                        dataStart.getTime());
                if (datas.size() == 0) {
                    ElectricData data = new ElectricData();
                    data.setDataType(DataType.T99);
                    data.setFieldProfile(GetField(fieldId));
                    data.setTime(dataStart.getTime());
                    data.setM5kW(BigDecimal.valueOf(Math.random() * 100 * 5 + 300));
                    data.setM2kW(BigDecimal.valueOf(Math.random() * 100 * 3 + 00));
                    data.setMsoc(BigDecimal.valueOf(50));
                    data.balance();
                    electricDataRepository.insert(data);
                }
                dataStart.add(Calendar.SECOND, model.timeInterval);
            }
        } catch (Throwable ex) {
            log.error(ex.getMessage(), ex);
        }
        RecommendControl result = service.MainControl(model);
        log.trace(result);
        // Assert.assertEquals(expected, actual);
    }
}
