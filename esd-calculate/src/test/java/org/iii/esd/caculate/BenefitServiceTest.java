package org.iii.esd.caculate;

import java.util.Date;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.benefit.BenefitInputModel;
import org.iii.esd.benefit.BenefitResult;
import org.iii.esd.benefit.BenefitService;
import org.iii.esd.benefit.BenefitSumResult;
import org.iii.esd.bill.BillServiceFactory;
import org.iii.esd.bill.Tph3sBillService;
import org.iii.esd.bill.Tpmrl2sBillService;
import org.iii.esd.enums.TouType;
import org.iii.esd.mongo.repository.ElectricDataRepository;
import org.iii.esd.mongo.service.ElectricDataService;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.mongo.service.UpdateService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {BenefitService.class, BillServiceFactory.class, Tpmrl2sBillService.class, Tph3sBillService.class,
        ElectricDataService.class, FieldProfileService.class, ElectricDataRepository.class, UpdateService.class})
@EnableAutoConfiguration
@Log4j2
public class BenefitServiceTest extends AbstractServiceTest {
    @Autowired
    private BenefitService service;

    private long profileId = 999L;
    private int dataType = 1;
    @SuppressWarnings("deprecation")
    private Date startDate = new Date("2017/01/01");
    @SuppressWarnings("deprecation")
    private Date endDate = new Date("2017/12/31");

    private double m3Capacity = 10;
    private double m6Capacity = 0;
    private double m7Capacity = 0;
    private double priceRatioBOU = 1;
    private double priceRatioPeak = 1;
    private double priceRatioOffPeak = 1;
    private double essSelfKWhComp = 0;
    private double essEffiComp = 1;

    private BenefitInputModel model = new BenefitInputModel(profileId, dataType, startDate, endDate, m3Capacity,
            m6Capacity, m7Capacity, priceRatioBOU, priceRatioPeak, priceRatioOffPeak,
            essSelfKWhComp, essEffiComp);

    @Test
    public void testTOUReduce() {
        BenefitResult benefitResult = service.getTOUReduce(model);

        if (benefitResult == null) { fail(); }

        log.info("tou.pv=" + benefitResult.getPv());
        log.info("tou.ess=" + benefitResult.getEss());
        log.info("tou.unload=" + benefitResult.getUnload());
        log.info("tou.total=" + benefitResult.getTotal());
        log.info("tou.dispatchBill=" + benefitResult.getDispatch_bill());
        log.info("tou.originalBill=" + benefitResult.getOriginal_bill());

        TouType whichTouType = service.DEFAULT_TOU_TYPE;
        if (whichTouType.equals(TouType.TPMRL2S)) {
            assertEquals(Double.valueOf(640816.24698), new Double(benefitResult.getPv()));
            assertEquals(Double.valueOf(0.0), new Double(benefitResult.getEss()));
            assertEquals(Double.valueOf(0.0), new Double(benefitResult.getUnload()));
            assertEquals(Double.valueOf(663856.24698), new Double(benefitResult.getTotal()));
            assertEquals(Double.valueOf(8234011.49802), new Double(benefitResult.getDispatch_bill()));
            assertEquals(Double.valueOf(8897867.745), new Double(benefitResult.getOriginal_bill()));
        } else if (whichTouType.equals(TouType.TPH3S)) {
            assertEquals(Double.valueOf(383018.96703), new Double(benefitResult.getPv()));
            assertEquals(Double.valueOf(0.0), new Double(benefitResult.getEss()));
            assertEquals(Double.valueOf(0.0), new Double(benefitResult.getUnload()));
            assertEquals(Double.valueOf(383018.96703), new Double(benefitResult.getTotal()));
            assertEquals(Double.valueOf(4668568.27497), new Double(benefitResult.getDispatch_bill()));
            assertEquals(Double.valueOf(5051587.242), new Double(benefitResult.getOriginal_bill()));
        } else { fail(); }
    }

    @Test
    public void testBOUReduce() {
        BenefitResult benefitResult = service.getBOUReduce(model);

        if (benefitResult == null) { fail(); }

        log.info("bou.pv=" + benefitResult.getPv());
        log.info("bou.ess=" + benefitResult.getEss());
        log.info("bou.unload=" + benefitResult.getUnload());
        log.info("bou.total=" + benefitResult.getTotal());
        log.info("bou.dispatchBill=" + benefitResult.getDispatch_bill());
        log.info("bou.originalBill=" + benefitResult.getOriginal_bill());

        TouType whichTouType = service.DEFAULT_TOU_TYPE;
        if (whichTouType.equals(TouType.TPMRL2S)) {
            assertEquals(Double.valueOf(0.0), new Double(benefitResult.getPv()));
            assertEquals(Double.valueOf(0.0), new Double(benefitResult.getEss()));
            assertEquals(Double.valueOf(0.0), new Double(benefitResult.getUnload()));
            assertEquals(Double.valueOf(0.0), new Double(benefitResult.getTotal()));
            assertEquals(Double.valueOf(900.0), new Double(benefitResult.getDispatch_bill()));
            assertEquals(Double.valueOf(900.0), new Double(benefitResult.getOriginal_bill()));
        } else if (whichTouType.equals(TouType.TPH3S)) {
            assertEquals(Double.valueOf(-78521.4611898), new Double(benefitResult.getPv()));
            assertEquals(Double.valueOf(0.0), new Double(benefitResult.getEss()));
            assertEquals(Double.valueOf(0.0), new Double(benefitResult.getUnload()));
            assertEquals(Double.valueOf(-84139.7574348), new Double(benefitResult.getTotal()));
            assertEquals(Double.valueOf(1615276.3915376998), new Double(benefitResult.getDispatch_bill()));
            assertEquals(Double.valueOf(1632020.7790275002), new Double(benefitResult.getOriginal_bill()));
        } else { fail(); }
    }

    @Test
    public void testSUM() {
        BenefitSumResult benefitSumResult = service.getSum(model);

        if (benefitSumResult == null) { fail(); }

        log.info("sum.pv=" + benefitSumResult.getSum().getPv());
        log.info("sum.ess=" + benefitSumResult.getSum().getEss());
        log.info("sum.unload=" + benefitSumResult.getSum().getUnload());
        log.info("sum.total=" + benefitSumResult.getSum().getTotal());
        log.info("sum.dispatchBill=" + benefitSumResult.getSum().getDispatch_bill());
        log.info("sum.originalBill=" + benefitSumResult.getSum().getOriginal_bill());

        TouType whichTouType = service.DEFAULT_TOU_TYPE;
        if (whichTouType.equals(TouType.TPMRL2S)) {
            assertEquals(Double.valueOf(640816.24698), new Double(benefitSumResult.getSum().getPv()));
            assertEquals(Double.valueOf(0.0), new Double(benefitSumResult.getSum().getEss()));
            assertEquals(Double.valueOf(0.0), new Double(benefitSumResult.getSum().getUnload()));
            assertEquals(Double.valueOf(663856.24698), new Double(benefitSumResult.getSum().getTotal()));
            assertEquals(Double.valueOf(8234911.49802), new Double(benefitSumResult.getSum().getDispatch_bill()));
            assertEquals(Double.valueOf(8898767.745), new Double(benefitSumResult.getSum().getOriginal_bill()));
        } else if (whichTouType.equals(TouType.TPH3S)) {
            assertEquals(Double.valueOf(304497.5058402), new Double(benefitSumResult.getSum().getPv()));
            assertEquals(Double.valueOf(0.0), new Double(benefitSumResult.getSum().getEss()));
            assertEquals(Double.valueOf(0.0), new Double(benefitSumResult.getSum().getUnload()));
            assertEquals(Double.valueOf(298879.2095952), new Double(benefitSumResult.getSum().getTotal()));
            assertEquals(Double.valueOf(6283844.6665076995), new Double(benefitSumResult.getSum().getDispatch_bill()));
            assertEquals(Double.valueOf(6683608.0210275), new Double(benefitSumResult.getSum().getOriginal_bill()));
        } else { fail(); }
    }
}
