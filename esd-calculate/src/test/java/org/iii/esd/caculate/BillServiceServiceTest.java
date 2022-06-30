package org.iii.esd.caculate;

import java.math.BigDecimal;
import java.util.Date;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.bill.BillServiceFactory;
import org.iii.esd.bill.BouFee;
import org.iii.esd.bill.IBillService;
import org.iii.esd.bill.TouFee;
import org.iii.esd.bill.Tph3sBillService;
import org.iii.esd.bill.Tpmrl2sBillService;
import org.iii.esd.enums.DataType;
import org.iii.esd.enums.TouType;
import org.iii.esd.exception.IiiException;
import org.iii.esd.mongo.service.ElectricDataService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {BillServiceFactory.class, Tpmrl2sBillService.class, Tph3sBillService.class,
        ElectricDataService.class})
@EnableAutoConfiguration
@Log4j2
class BillServiceServiceTest extends AbstractServiceTest {

    @SuppressWarnings("deprecation")
    Date Start_Day = new Date("2017/01/01");
    @SuppressWarnings("deprecation")
    Date End_Day = new Date("2017/12/31");
    long Profile_id = 999L;
    @Autowired
    private BillServiceFactory billServiceFactory;
    private int historyDataType = DataType.T1.getCode();
    private int peakclippingDataType = DataType.T3.getCode();

    @Test
    public void TPH3S_M1_M2_TOUTest() {
        IBillService service = billServiceFactory.GetInstance(TouType.TPH3S);
        TouFee result = service.TimeOfUsed_Bill(Start_Day, End_Day, Profile_id, historyDataType, 1, 1, 0, 1);
        log.trace("Start:{},End:{},result:{}", Start_Day, End_Day, result);
        assertEquals(BigDecimal.valueOf(383442),
                result.getTotal_Bill_M2_KWH().setScale(0, BigDecimal.ROUND_HALF_UP));
        assertEquals(BigDecimal.valueOf(4674351),
                result.getTotal_Bill_M1_KWH().setScale(0, BigDecimal.ROUND_HALF_UP));

    }

    @Test
    public void TPH3S_M1_M2_BOUTest() {
        IBillService service = billServiceFactory.GetInstance(TouType.TPH3S);
        BouFee result = service.BOU_PF_Bill(Start_Day, End_Day, Profile_id, historyDataType, 1, 1, 1, 0, 1);
        log.trace("Start:{},End:{},result:{}", Start_Day, End_Day, result);
        BigDecimal boubenefit = result.getBOU_Bill_M1_M2_M3_M7().subtract(result.getPF_Bill_M1_M2_M3_M7())
                                      .subtract(result.getBOU_Bill_M1()).add(result.getPF_Bill_M1()).setScale(0, BigDecimal.ROUND_HALF_UP);
        assertEquals(BigDecimal.valueOf(16738), boubenefit);
    }

    @Test
    public void TPMRL2S_M1_M2_TOUTest() {
        IBillService service = billServiceFactory.GetInstance(TouType.TPMRL2S);
        TouFee result = service.TimeOfUsed_Bill(Start_Day, End_Day, Profile_id, historyDataType, 1, 1, 0, 1);
        log.trace("Start:{},End:{},result:{}", Start_Day, End_Day, result);
        TouFee result2 = service.TimeOfUsed_Bill(Start_Day, End_Day, Profile_id, peakclippingDataType, 1, 1, 0, 1);
        log.trace("Start:{},End:{},result:{}", Start_Day, End_Day, result);
        assertEquals(result.getTotal_Bill_M2_KWH().setScale(0, BigDecimal.ROUND_HALF_UP),
                result2.getTotal_Bill_M2_KWH().setScale(0, BigDecimal.ROUND_HALF_UP));
        assertEquals(BigDecimal.valueOf(8234011),
                result.getTotal_Bill_M1_KWH().setScale(0, BigDecimal.ROUND_HALF_UP));
        assertEquals(BigDecimal.valueOf(8897868),
                result.getTotal_Bill_B_KWH().setScale(0, BigDecimal.ROUND_HALF_UP));
        // log.info(service.BOU_PF_Bill(Start_Day, End_Day, Profile_id, historyDataType,
        // 1, 1, 1, 0, 1));
    }

    @Test
    public void TPMRL2S_M1_M2_BOUTest() {
        IBillService service = billServiceFactory.GetInstance(TouType.TPMRL2S);
        // log.info(service.TimeOfUsed_Bill(Start_Day, End_Day, Profile_id,
        // historyDataType, 1, 1, 0, 1));
        BouFee result = service.BOU_PF_Bill(Start_Day, End_Day, Profile_id, historyDataType, 1, 1, 1, 0, 1);
        log.trace("Start:{},End:{},result:{}", Start_Day, End_Day, result);
        assertEquals(result.getBOU_Bill_M1(), result.getBOU_Bill_M1_M2_M3_M7());
    }

    /**
     * 不檢查結果，單純測試錯誤
     */
    @SuppressWarnings("deprecation")
    @Test
    public void TOUhanaiTest() {
        Date date = new Date("1911/01/01");
        for (TouType type : TouType.values()) {
            IBillService service = billServiceFactory.GetInstance(type);
            try {
                service.BOU_PF_Bill(date, End_Day, Profile_id, historyDataType, 1, 1, 1, 0, 1);
            } catch (IiiException e) {

            } catch (Exception e) {
                fail();
            }
            try {
                service.TimeOfUsed_Bill(date, End_Day, Profile_id, historyDataType, 1, 1, 0, 1);
            } catch (IiiException e) {

            } catch (Exception e) {
                fail();
            }
        }

    }
}
