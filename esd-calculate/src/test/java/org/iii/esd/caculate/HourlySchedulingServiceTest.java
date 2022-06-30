package org.iii.esd.caculate;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.enums.DataType;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.PolicyProfile;
import org.iii.esd.mongo.repository.ElectricDataRepository;
import org.iii.esd.mongo.service.ElectricDataService;
import org.iii.esd.schedule.HourlySchedulingService;
import org.iii.esd.schedule.SchedulingInputModel;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {HourlySchedulingService.class, ElectricDataService.class})
@EnableAutoConfiguration
@Log4j2
class HourlySchedulingServiceTest extends AbstractServiceTest {


    @Autowired
    ElectricDataRepository electricDataRepository;
    @Autowired
    ElectricDataService electricDataService;
    @SuppressWarnings("deprecation")
    Date historyStartDate = new Date("2017/08/08");
    @SuppressWarnings("deprecation")
    Date scheduleStartDate = new Date("2017/08/09");
    @SuppressWarnings("deprecation")
    Date clippingStartDate = new Date("2017/01/01");
    @SuppressWarnings("deprecation")
    Date interTestDate = new Date("2017/07/12");
    Long clippingFieldId = 999L;
    private Long fieldId = 999L;
    @Autowired
    private HourlySchedulingService service;

    FieldProfile GetField() {
        return new FieldProfile(fieldId);
    }

    /**
     * 銷峰
     *
     * @throws Exception
     */
    @Test
    @Order(1)
    void PeakClipping() throws Exception {
        /**
         * 開啟DEBUG模式，檢查用電資料是否正常
         * 檢查的時候只檢查排程指定時段後的資料
         * 可以用field-mothMax-aggregate.js檢查各月M3充放電總計
         */

        service.DEBUG_MODE = true;

        Calendar start = Utility.GetControlDay(clippingStartDate);
        Calendar end = Utility.GetControlDay(clippingStartDate);
        end.add(Calendar.YEAR, 1);
        SchedulingInputModel model = new SchedulingInputModel();

        model.setFieldId(clippingFieldId);
        model.setFORECAST_DATA_TYPE(DataType.T1.getCode());
        model.setREAL_DATA_TYPE(DataType.T3.getCode());
        model.setSCHEDULE_DATA_TYPE(DataType.T3.getCode());
        model.setBattery_Capacity(BigDecimal.valueOf(50.0 * 0.95));
        model.setMax_Power(BigDecimal.valueOf(25));
        model.setCharge_Efficiency(BigDecimal.valueOf(0.934));
        model.setDischarge_Efficiency(BigDecimal.valueOf(0.934));
        // 做銷峰就別設定契約容量
        //model.setTYOD(BigDecimal.valueOf(0));// 重要
        //model.setConfig(PolicyProfile.PeakClippingDefault());
        model.ChangePolicyToPeakClipping();
        while (start.compareTo(end) < 0) { // 執行需要的參數，還有對應的參數檢查FUNCTION
            model.setSchedule_Start(start.getTime());
            model.check();
            service.SchedulingByStrategy(model);
            start.add(Calendar.DAY_OF_YEAR, 1);
        }

        // fail();
    }

    /**
     * 檢查銷峰資料正確性
     *
     * @throws Exception
     */
    @Test
    @Order(2)
    void PeakClippingResultTest() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Field:" + clippingFieldId + "\n");
        try {
            Calendar start = Utility.GetControlDay(clippingStartDate);
            Calendar end = Utility.GetControlDay(clippingStartDate);
            end.add(Calendar.YEAR, 1);
            while (start.compareTo(end) < 0) {
                Calendar month1st = (Calendar) start.clone();
                Calendar monthlast = (Calendar) month1st.clone();
                monthlast.add(Calendar.MONTH, 1);
                BigDecimal history = electricDataService.findByFieldIdAndDataTypeAndTimeRangeOrderByM1kWDesc(
                        clippingFieldId, DataType.T1, month1st.getTime(), monthlast.getTime());
                BigDecimal clipping = electricDataService.findByFieldIdAndDataTypeAndTimeRangeOrderByM1kWDesc(
                        clippingFieldId, DataType.T3, month1st.getTime(), monthlast.getTime());
                sb.append(String.format("%s - %s => %.2f, %.2f\n", month1st.getTime(), monthlast.getTime(), history,
                        clipping));

                if (clipping.compareTo(history) > 0) {
                    fail();
                }
                start = monthlast;
            }
        } catch (Throwable ex) {
            throw ex;
        } finally {
            log.info(sb.toString());
        }
    }

    /**
     * 前置排程
     *
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    @Test
    @Order(3)
    void DayScheduleTest() throws Exception {

        Calendar start = Calendar.getInstance();
        start.setTime(new Date("2017/01/01"));
        SchedulingInputModel model = new SchedulingInputModel();

        model.setFieldId(999);
        model.setFORECAST_DATA_TYPE(DataType.T1.getCode());
        model.setREAL_DATA_TYPE(DataType.T2.getCode());
        model.setSCHEDULE_DATA_TYPE(DataType.T2.getCode());
        model.setBattery_Capacity(BigDecimal.valueOf(8.31));
        model.setMax_Power(BigDecimal.valueOf(10));
        model.setCharge_Efficiency(BigDecimal.valueOf(0.934));
        model.setDischarge_Efficiency(BigDecimal.valueOf(0.934));
        model.setTYOD(BigDecimal.valueOf(723));
        model.setConfig(PolicyProfile.Default());
        for (int i = 1; i <= 365; i++) { // 執行需要的參數，還有對應的參數檢查FUNCTION
            model.setSchedule_Start(start.getTime());
            model.check();
            service.SchedulingByStrategy(model);
            start.add(Calendar.DAY_OF_YEAR, 1);
        }
        // fail();
    }

    /**
     * 調度排程測試方法還沒想好，一種是中間時段開始執行的每小時排程，愣外一種是全天的
     * 全天的部分與前置排程類似
     *
     * @throws Exception
     */
    @Test
    @Order(4)
    void HourlyScheduleTest() throws Exception {

        Calendar ic = Calendar.getInstance();
        ic.setTime(interTestDate);
        // 執行需要的參數，還有對應的參數檢查FUNCTION
        // 當天00:00執行一次
        SchedulingInputModel model = new SchedulingInputModel();
        model.setSchedule_Start(ic.getTime());
        model.setFieldId(clippingFieldId);
        // 因為資料問題，所以用T1當預測來源、T3當實際資料、T11為排程輸出結果
        // 實際運轉應該是用T10當預測來源、T1當實際資料、T11為排程輸出結果
        model.setFORECAST_DATA_TYPE(DataType.T1.getCode()); // T10
        model.setREAL_DATA_TYPE(DataType.T3.getCode()); // T1
        model.setSCHEDULE_DATA_TYPE(DataType.T11.getCode()); //T11
        model.setBattery_Capacity(BigDecimal.valueOf(50));
        model.setMax_Power(BigDecimal.valueOf(25));
        model.setCharge_Efficiency(BigDecimal.valueOf(0.9));
        model.setDischarge_Efficiency(BigDecimal.valueOf(0.9));
        model.setTYOD(BigDecimal.valueOf(300));
        model.setConfig(PolicyProfile.Default());
        model.check();
        service.SchedulingByStrategy(model);

        // 同樣的資料 當天15:00再執行一次

        ic.add(Calendar.HOUR_OF_DAY, 15);
        model.setSchedule_Start(ic.getTime());
        model.check();
        service.SchedulingByStrategy(model);
        // fail();
    }

}
