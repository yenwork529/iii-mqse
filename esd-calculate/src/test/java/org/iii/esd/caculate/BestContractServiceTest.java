package org.iii.esd.caculate;

import java.util.ArrayList;
import java.util.Date;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.bill.BillServiceFactory;
import org.iii.esd.bill.Tph3sBillService;
import org.iii.esd.bill.Tpmrl2sBillService;
import org.iii.esd.contract.BestContractService;
import org.iii.esd.contract.BestContractTable;
import org.iii.esd.contract.MonthlyPowerData;
import org.iii.esd.mongo.repository.TouOfTPH3SRepository;
import org.iii.esd.mongo.service.ElectricDataService;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.mongo.service.UpdateService;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {BestContractService.class, ElectricDataService.class, TouOfTPH3SRepository.class,
        FieldProfileService.class, BillServiceFactory.class, UpdateService.class, Tpmrl2sBillService.class,
        Tph3sBillService.class})
@EnableAutoConfiguration
@Log4j2
public class BestContractServiceTest extends AbstractServiceTest {
    @Autowired
    private BestContractService service;

    private long profileId = 999L;
    private int dataType = 1;
    @SuppressWarnings("deprecation")
    private Date startDate = new Date("2017/01/01");
    @SuppressWarnings("deprecation")
    private Date endDate = new Date("2017/12/31");
    private long contract = 947L;
    private String bestContractType = "B"; // A, B, A2

    private double priceRatioPeak = 1;
    private double priceRatioOffPeak = 1;
    private double essSelfKWhComp = 0;
    private double essEffiComp = 1;

    @Test
    public void testGetYearReading() {
        //注意startDate與endDate必須為一整年的區間
        ArrayList<MonthlyPowerData> nTableList = service.getYearReading(startDate, endDate, profileId, contract,
                bestContractType, dataType);
        assertTrue(nTableList.size() == 12);
    }

    @Test
    public void testGetBestContract() {
        //注意startDate與endDate必須為一整年的區間
        BestContractTable bestContractTable = service.getBestContract(startDate, endDate, profileId, contract,
                bestContractType, dataType, priceRatioPeak, priceRatioOffPeak, essSelfKWhComp, essEffiComp);
        assertTrue(bestContractTable != null);
        assertTrue(bestContractTable.getBestContractCapacity() == 768.0);  //新經常契約容量
        assertTrue(bestContractTable.getBestBaseCost() == 1712332.8);  //新基本電費
        assertTrue(bestContractTable.getBestOverCost() == 0.0);  //新超約電費
        assertTrue(bestContractTable.getBestPfCost() == 89796.29780249999);  //新PF調整費
        assertTrue(bestContractTable.getBestUsedCost() == 5057792.363999999);  //新流動電費
        assertTrue(bestContractTable.getOrigBaseCost() == 2111431.2);  //目前基本電費
        assertTrue(bestContractTable.getOrigOverCost() == 0.0);  //目前超約電費
        assertTrue(bestContractTable.getOrigPfCost() == 107538.35346);  //目前PF調整費
        assertTrue(bestContractTable.getOrigUsedCost() == 5057792.363999999);  //目前流動電費
        log.info(bestContractTable.toString());
    }
}
