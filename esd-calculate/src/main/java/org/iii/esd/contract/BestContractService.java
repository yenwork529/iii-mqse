package org.iii.esd.contract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.iii.esd.bill.BillServiceFactory;
import org.iii.esd.bill.IBillService;
import org.iii.esd.bill.TouFee;
import org.iii.esd.caculate.Utility;
import org.iii.esd.enums.DataType;
import org.iii.esd.enums.TouType;
import org.iii.esd.exception.IiiException;
import org.iii.esd.mongo.document.AbstractTou;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.TouOfTPH3S;
import org.iii.esd.mongo.repository.TouOfTPH3SRepository;
import org.iii.esd.mongo.service.ElectricDataService;
import org.iii.esd.mongo.service.FieldProfileService;

/***
 * 對應原C#版本BestContract專案的最佳契約容量相關演算法，<br>
 * 參考BestContract\Service\BestContractService.cs進行改寫<br>
 *
 * @author willhahn
 *
 */
@Service
@Log4j2
public class BestContractService {

    public final TouType DEFAULT_TOU_TYPE = TouType.TPH3S;
    @Autowired
    private ElectricDataService electricDataService;
    @Autowired
    private TouOfTPH3SRepository touOfTPH3SRepository;
    @Autowired
    private FieldProfileService fieldProfileService;
    @Autowired
    private BillServiceFactory billServiceFactory;

    // 抓用電資料
    // TODO: C#版本演算法在for迴圈處寫死固定從一月份開始，先照翻，未來再確認是否會產生bug
    public ArrayList<MonthlyPowerData> getYearReading(Date startTime, Date endTime, long fieldId, double contract, String bestContractType,
            int dataType) {
        List<TouOfTPH3S> tous = touOfTPH3SRepository.findAll();
        ArrayList<MonthlyPowerData> nTableList = new ArrayList<MonthlyPowerData>();
        //基本電費
        double Regular_Contract_Cost = 0;  //經常契約電費單價
        int months = Utility.countMonth(startTime, endTime);
        for (int m = 1; m <= months; m++) {  //選擇時間有幾個月
            int year = Utility.getCalendar(Utility.addMonths(startTime, m - 1)).get(Calendar.YEAR);
            int month = Utility.getCalendar(Utility.addMonths(startTime, m - 1)).get(Calendar.MONTH);
            Date SearchMonth_StartDate = new GregorianCalendar(year, month, 1, 0, 15, 0).getTime();  //月第一天從15分開始才是本月
            Date SearchMonth_EndDate = Utility.addMinutes(Utility.addMonths(SearchMonth_StartDate, 1), -15);  //下個月00:00即是本月最後一筆

            // 依照日期區間決定使用電價版本
            Optional<TouOfTPH3S> ouseTou = tous.stream()
                                               .filter(a -> a.getActiveTime().compareTo(SearchMonth_StartDate) <= 0)
                                               .sorted(Comparator.comparing(AbstractTou::getActiveTime).reversed()).findFirst();
            if (!ouseTou.isPresent()) {
                log.error("查無對應區間電價:startTime=" + startTime + "&endTime=" + endTime + "&fieldId=" + fieldId + "&contract=" + contract +
                        "&bestContractType=" + bestContractType + "&dataType=" + dataType);
                throw new IiiException("查無對應區間電價");
            }
            TouOfTPH3S useTou = ouseTou.get();
            //取出電價(用月份判斷就好)
            if ((Utility.getCalendar(SearchMonth_StartDate).get(Calendar.MONTH) + 1) >= 6 &&
                    (Utility.getCalendar(SearchMonth_StartDate).get(Calendar.MONTH) + 1) <= 9) {
                Regular_Contract_Cost = useTou.getSummer_Regular_Contarct().doubleValue();
            } else {
                Regular_Contract_Cost = useTou.getNonSummer_Regular_Contarct().doubleValue();
            }
            double Max_15Min_KW = 0;
            //最高需量
            try {
                if (bestContractType.equals("A")) {
                    Max_15Min_KW = electricDataService
                            .FindMaxM5addM6subM2subM3subM8subM9subM10InTimeRangeInDocument(fieldId, DataType.getCode(dataType),
                                    SearchMonth_StartDate, SearchMonth_EndDate).getKW().doubleValue();
                } else if (bestContractType.equals("B")) {
                    Max_15Min_KW = electricDataService
                            .FindMaxM5addM6addM7InTimeRangeInDocument(fieldId, DataType.getCode(dataType), SearchMonth_StartDate,
                                    SearchMonth_EndDate).getKW().doubleValue();
                } else if (bestContractType.equals("A2")) {
                    Max_15Min_KW = electricDataService
                            .FindMaxM5addM6subM3subM8subM9subM10InTimeRangeInDocument(fieldId, DataType.getCode(dataType),
                                    SearchMonth_StartDate, SearchMonth_EndDate).getKW().doubleValue();
                }
                int searchMonth = Utility.getCalendar(SearchMonth_StartDate).get(Calendar.MONTH) + 1;
                MonthlyPowerData nTable = new MonthlyPowerData();
                nTable.setSearchStartMonth(Utility.getCalendar(SearchMonth_StartDate).get(Calendar.YEAR) + "/" + searchMonth);
                nTable.setMax15MinKW(Max_15Min_KW);
                nTable.setContract(contract);
                nTable.setRegularContractCost(Regular_Contract_Cost);
                nTableList.add(nTable);
            } catch (Exception ex) {
                log.fatal("fieldId=" + fieldId + "&SearchMonth_StartDate=" + SearchMonth_StartDate + "&SearchMonth_EndDate=" +
                        SearchMonth_EndDate + "&dataType=" + dataType + "&bestContractType=" + bestContractType + "&ex=" + ex.getMessage());
            }
        }
        return nTableList;
    }

    /***最佳契約容量A-TYOD(用ESS削峰)--以(所有電器負載-PV發電量)之值計算M5+M6+M7-M3-M2
     * 計算最佳契約容量
     * @param startTime 開始日
     * @param endTime 結束日
     * @param fieldId
     * @param contractSet 原契約容量設定
     * @param bestContractType A, A2, B
     * @param dataType 使用何總資料類型
     * @param priceRatioPeak 流動電費尖峰,半尖峰倍率
     * @param priceRatioOffPeak 流動電費離峰倍率
     * @param essSelfkWh ESS自放度數
     * @param essEffi ESS補償轉換效率
     * @return
     */
    public BestContractTable getBestContract(Date startTime, Date endTime, long fieldId, double contractSet, String bestContractType,
            int dataType, double priceRatioPeak, double priceRatioOffPeak, double essSelfkWh, double essEffi) {
        //從資料庫撈個月數值(最高需量、基本電費單價、經常契約容量)
        ArrayList<MonthlyPowerData> bestContractTableList =
                getYearReading(startTime, endTime, fieldId, contractSet, bestContractType, dataType);

        //流動電費(因為是固定的，所以另外先算)
        double[] usedBillArray = new double[Utility.countMonth(startTime, endTime)];
        Optional<FieldProfile> fieldProfile = fieldProfileService.find(fieldId);
        if (!fieldProfile.isPresent()) {
            return null;
        }
        TouType touType = fieldProfile.get().getTouType();
        IBillService billService = billServiceFactory.GetInstance(touType != null ? touType : DEFAULT_TOU_TYPE);

        for (int i = 1; i <= Utility.countMonth(startTime, endTime); i++) {
            int year = Utility.getCalendar(Utility.addMonths(startTime, i - 1)).get(Calendar.YEAR);
            int month = Utility.getCalendar(Utility.addMonths(startTime, i - 1)).get(Calendar.MONTH);

            Date startDate = new GregorianCalendar(year, month, 1, 0, 0, 0).getTime();    //每個月第一天，nTable.Rows[0]放的是Year/Month
            Date endDate = Utility.addDays(Utility.addMonths(startDate, 1), -1);   //每個月最後一天(下個月第一天減一天)
            TouFee touFee = billService
                    .TimeOfUsed_Bill(startDate, endDate, fieldId, dataType, priceRatioPeak, priceRatioOffPeak, essSelfkWh, essEffi);

            //放到陣列
            if (bestContractType.equals("A")) {
                usedBillArray[i - 1] += touFee.getTotal_Bill_A_KWH().doubleValue();
            } else if (bestContractType.equals("B")) {
                usedBillArray[i - 1] += touFee.getTotal_Bill_B_KWH().doubleValue();
            } else if (bestContractType.equals("A2")) { usedBillArray[i - 1] += touFee.getTotal_Bill_A2_KWH().doubleValue(); }
        }

        BestContractTable bestContractTable = new BestContractTable();
        bestContractTable.setDeviceName(Long.toString(fieldId));
        //最低基本電費(基本+超約)經常契約
        ArrayList<BestCostTable> contractMoneyTable = bestBaseCost(bestContractTableList, usedBillArray, startTime);
        BestCostTable bestCostTable = sortAndGetBestCostTable(contractMoneyTable);
        bestContractTable.setBestContractCapacity(bestCostTable.getTestContract());
        bestContractTable.setBestBaseCost(bestCostTable.getBaseCost());
        bestContractTable.setOrigOverCost(bestCostTable.getOverCost());
        bestContractTable.setBestUsedCost(bestCostTable.getUsedCost());
        bestContractTable.setBestPfCost(bestCostTable.getPfCost());
        //目前經常契約
        BaseCostTable powerBaseCost = calculateBaseCost(bestContractTableList, usedBillArray, startTime);
        bestContractTable.setOrigBaseCost(powerBaseCost.getBaseCost());
        bestContractTable.setOrigOverCost(powerBaseCost.getOverCost());
        bestContractTable.setOrigUsedCost(powerBaseCost.getUsedCost());
        bestContractTable.setOrigPfCost(powerBaseCost.getPfCost());
        return bestContractTable;
    }

    //計算出最佳契約容量，最佳契約容量算法是用假設的契約容量範圍去做迴圈試算
    public ArrayList<BestCostTable> bestBaseCost(ArrayList<MonthlyPowerData> nTableList, double[] usedBillArray, Date startTime) {
        //TODO: 之後再確認既有程式取nTable.Rows[2]["1"]的原因
        //		long ContractSet = Convert.ToInt64(nTable.Rows[2]["1"].ToString()); //經常契約容量
        double ContractSet = nTableList.get(1).getContract();
        double Month_Max_kW = 0;
        double Month_Contract = 0;
        double Month_Price = 0;
        double BaseCost = 0; //基本電費
        double OverCost = 0; //基本超約電費
        double UsedCost = 0; //流動電費
        double PfCost = 0; //功率因數調整費

        List<TouOfTPH3S> tous = touOfTPH3SRepository.findAll();
        ArrayList<BestCostTable> bestCostTableList = new ArrayList<BestCostTable>();
        double Max_kW = 0;
        double Min_kW = 999999999;
        //先找出最大需量
        for (int i = 0; i < nTableList.size(); i++) {
            Month_Max_kW = nTableList.get(i).getMax15MinKW();
            if (Month_Max_kW > Max_kW) { Max_kW = Month_Max_kW; }
            if (Month_Max_kW < Min_kW) { Min_kW = Month_Max_kW; }
        }
        //若最大需量小於契約容量，那要試算的假設契約容量上限為契約容量，反之為最大需量
        double Max_contract = roundEven(Max_kW);
        if (Max_contract < ContractSet) { Max_contract = ContractSet; }
        //若最大需量大於契約容量，那試算的契約容量下限為契約容量，反之為最小需量
        double Min_contract = roundEven(Min_kW);
        if (Min_contract > ContractSet) { Min_contract = ContractSet; }

        //設置契約容量範圍跑迴圈
        for (double Test_contract = Min_contract; Test_contract <= Max_contract; Test_contract++) {
            BaseCost = 0;
            OverCost = 0;
            UsedCost = 0;
            PfCost = 0;

            BestCostTable bestCostTable = new BestCostTable();
            bestCostTable.setTestContract(Test_contract);

            for (int i = 1; i <= nTableList.size(); i++) {
                int year = Utility.getCalendar(Utility.addMonths(startTime, i - 1)).get(Calendar.YEAR);
                int month = Utility.getCalendar(Utility.addMonths(startTime, i - 1)).get(Calendar.MONTH);
                Date SearchMonth_StartDate = new GregorianCalendar(year, month, 1, 0, 15, 0).getTime();  //月第一天從15分開始才是本月
                // 依照日期區間決定使用電價版本
                Optional<TouOfTPH3S> ouseTou = tous.stream()
                                                   .filter(a -> a.getActiveTime().compareTo(SearchMonth_StartDate) <= 0)
                                                   .sorted(Comparator.comparing(AbstractTou::getActiveTime).reversed()).findFirst();
                if (!ouseTou.isPresent()) {
                    throw new IiiException("查無對應區間電價");
                }
                TouOfTPH3S useTou = ouseTou.get();
                Month_Max_kW = nTableList.get(i - 1).getMax15MinKW();
                Month_Contract = (double) nTableList.get(i - 1).getContract();
                Month_Price = nTableList.get(i - 1).getRegularContractCost();
                BaseCost += Test_contract * Month_Price;
                //若超約
                OverCost += calculateOverCost(Month_Max_kW, Test_contract, Month_Price);
                //流動電費
                UsedCost += usedBillArray[i - 1];
                //功率因數調整費(總電費*2.7%(以pf=98-80=18*0.15=2.7，或超約=0))
                if (calculateOverCost(Month_Max_kW, Test_contract, Month_Price) > 0) { PfCost += 0; } else {
                    PfCost += ((Test_contract * Month_Price) + usedBillArray[i - 1]) * useTou.getPF_Adj().doubleValue();
                }
            }
            bestCostTable.setBaseCost(BaseCost);
            bestCostTable.setOverCost(OverCost);
            bestCostTable.setUsedCost(UsedCost);
            bestCostTable.setPfCost(PfCost);
            bestCostTable.setTotal(BaseCost + OverCost + UsedCost - PfCost);
            bestCostTableList.add(bestCostTable);
        }
        return bestCostTableList;
    }

    //計算基本電費(超約電費部分)
    private double calculateOverCost(double Month_Max_kW, double Contract, double Price) {
        double OverCost = 0;
        if (Month_Max_kW > Contract) {
            double Over_10_up_Contract = 0;
            double Over_10_low_Contract = 0;
            if (Month_Max_kW > Contract * 1.1) {
                Over_10_up_Contract = Month_Max_kW - Contract * 1.1;  //超過10%的部分
            }
            Over_10_low_Contract = Month_Max_kW - Contract - Over_10_up_Contract; //超過經常契約10%以下的部分
            //10%以下
            if (Over_10_low_Contract != 0) {
                OverCost += Price * 2 * Over_10_low_Contract;
            }
            //超過10%
            if (Over_10_up_Contract > 0) {
                OverCost += Price * 3 * Over_10_up_Contract;
            }
        }
        return OverCost;
    }

    //計算基本電費(基本電費部分)
    private BaseCostTable calculateBaseCost(ArrayList<MonthlyPowerData> nTableList, double[] usedBillArray, Date startTime) {
        double Month_Max_kW = 0;
        double Month_Contract = 0;
        double Month_Price = 0;
        double BaseCost = 0; //基本電費
        double OverCost = 0; //超約基本電費
        double UsedCost = 0; //流動電費
        double PfCost = 0; //功率因數調整費

        List<TouOfTPH3S> tous = touOfTPH3SRepository.findAll();
        BaseCostTable baseCostTable = new BaseCostTable();

        for (int m = 1; m <= nTableList.size(); m++) {
            int year = Utility.getCalendar(Utility.addMonths(startTime, m - 1)).get(Calendar.YEAR);
            int month = Utility.getCalendar(Utility.addMonths(startTime, m - 1)).get(Calendar.MONTH);
            Date SearchMonth_StartDate = new GregorianCalendar(year, month, 1, 0, 15, 0).getTime();  //月第一天從15分開始才是本月
            // 依照日期區間決定使用電價版本
            Optional<TouOfTPH3S> ouseTou = tous.stream()
                                               .filter(a -> a.getActiveTime().compareTo(SearchMonth_StartDate) <= 0)
                                               .sorted(Comparator.comparing(AbstractTou::getActiveTime).reversed()).findFirst();
            if (!ouseTou.isPresent()) {
                throw new IiiException("查無對應區間電價");
            }
            TouOfTPH3S useTou = ouseTou.get();

            Month_Max_kW = nTableList.get(m - 1).getMax15MinKW();
            Month_Contract = (double) nTableList.get(m - 1).getContract();
            Month_Price = nTableList.get(m - 1).getRegularContractCost();
            BaseCost += Month_Contract * Month_Price;
            //若超約
            OverCost += calculateOverCost(Month_Max_kW, Month_Contract, Month_Price);
            //流動電費
            UsedCost += usedBillArray[m - 1];
            //功率因數調整費(總電費*2.7%(以pf=98-80=18*0.15=2.7，或超約=0))
            if (calculateOverCost(Month_Max_kW, Month_Contract, Month_Price) > 0) { PfCost += 0; } else {
                PfCost += ((Month_Contract * Month_Price) + usedBillArray[m - 1]) * useTou.getPF_Adj().doubleValue();
            }
        }
        baseCostTable.setBaseCost(BaseCost);
        baseCostTable.setOverCost(OverCost);
        baseCostTable.setUsedCost(UsedCost);
        baseCostTable.setPfCost(PfCost);
        baseCostTable.setTotal(BaseCost + OverCost + UsedCost - PfCost);
        return baseCostTable;
    }

    private double roundEven(double value) {
        return Math.round(value / 2) * 2;
    }

    private BestCostTable sortAndGetBestCostTable(ArrayList<BestCostTable> data) {
        if (data == null || data.size() == 0) { return null; }

        log.info("[before sorting] best cost table(total)=" + data.get(0).getTotal());
        Collections.sort(data);
        log.info("[after sorting] best cost table(total)=" + data.get(0).getTotal());
        return data.get(0);
    }
}