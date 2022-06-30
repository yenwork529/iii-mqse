package org.iii.esd.sr.service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;
// import org.iii.esd.mongo.service.integrate.TxgFieldProfileService;
// import org.iii.esd.mongo.service.integrate.TxgSpinReserveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.iii.esd.enums.AncillaryServiceType;
import org.iii.esd.enums.DataType;
import org.iii.esd.enums.NoticeType;
import org.iii.esd.enums.StatisticsType;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.SpinReserveBid;
import org.iii.esd.mongo.document.SpinReserveBidDetail;
import org.iii.esd.mongo.document.SpinReserveData;
import org.iii.esd.mongo.document.SpinReserveDataDetail;
import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.mongo.document.SpinReserveStatistics;
import org.iii.esd.mongo.document.SpinReserveStatisticsDetail;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.mongo.service.SpinReserveService;
import org.iii.esd.mongo.service.StatisticsService;

/***
 * SR、SUP 容量費用、電能費用與執行率計算，<br>
 *
 * @author SWL、PIZZA
 *
 */
@Service
@Log4j2
public class SrSupRevenueService {

    @Autowired
    private SpinReserveService spinReserveService;

    // @Autowired
    // private TxgSpinReserveService txgSpinReserveService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private FieldProfileService fieldProfileService;

    // @Autowired
    // private TxgFieldProfileService txgFieldProfileService;

    /***
     * 單次調度執行結果參數計算(服務能量、執行率、每小時服務電能量、服務品質指標)
     *
     * @param srId        srId
     * @param startTime    起始時間
     * @param endTime    結束時間
     * @Param serviceType    服務類型
     *
     */
    public void serviceParametersCalculate(Long srId, Date startTime, Date endTime, AncillaryServiceType serviceType) {

        Boolean flag = false;

        //時段內srId的EnergyRevenue計算

        List<SpinReserveData> spinReserveDataList = this.getEnergy(srId,
                spinReserveService.findSpinReserveDataBySrIdAndNoticeTypeAndNoticeTime(srId, NoticeType.UNLOAD, startTime, endTime), serviceType);

        List<SpinReserveData> newSpinReserveDataList = this.getProformance(srId, spinReserveDataList, AncillaryServiceType.sr);

        if (newSpinReserveDataList != null && newSpinReserveDataList.size() > 0) {
            //Save spinReserveDataList
            spinReserveService.saveSpinReserveData(newSpinReserveDataList);
        }

        List<SpinReserveBid> spinReserveBid = this.getHourlyServiceEnergy(srId, newSpinReserveDataList, AncillaryServiceType.sr);

        if (spinReserveBid != null && spinReserveBid.size() > 0) {
            //Save newSpinReserveBid
            spinReserveService.addOrUpdateAll(srId, spinReserveBid);
        }

        List<SpinReserveBid> newSpinReserveBid = this.getServiceFactor(srId, newSpinReserveDataList, AncillaryServiceType.sr);

        if (newSpinReserveBid != null && newSpinReserveBid.size() > 0) {
            //Save newSpinReserveBid
            spinReserveService.addOrUpdateAll(srId, newSpinReserveBid);
        }

    }


    /***
     * 所有TXG(Srid)之SR與SUP計算容量費、效能費 與 電能費 計算
     *
     * @param startTime     起始時間
     * @param endTime       結束時間
     * @return boolean
     */
    public Boolean revenueCalculate(Date startTime, Date endTime) {

        Boolean flag = false;

        Calendar start = Calendar.getInstance();

        start.setTime(startTime);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MILLISECOND, 0);
        startTime = start.getTime();

        List<SpinReserveProfile> list = spinReserveService.findAllSpinReserveProfile();
        Long srId;

        for (SpinReserveProfile srList : list) {

            srId = srList.getId();

            List<SpinReserveBid> spinReserveBidList = this.getCapacityRevenue(spinReserveService.findAllBySrIdAndTime(srId, startTime));

            List<SpinReserveBid> spinReserveBidList1 = this.getEfficacyRevenue(spinReserveBidList);

            List<SpinReserveBid> spinReserveBidList2 = this.getEnergyRevenue(spinReserveBidList1);

            if (spinReserveBidList2 != null && spinReserveBidList2.size() > 0) {
                //Save spinReserveBidList
                spinReserveService.addOrUpdateAll(srId, spinReserveBidList2);
            }

        }

        return flag;
    }


    /***
     * 單一TXG(Srid)之計算容量費、效能費 與 電能費 計算
     *
     * @param srId      SrID
     * @param startTime 起始時間
     * @param endTime   結束時間
     * @return boolean
     * @Sam 20201022
     */
    public void revenueCalculateBySridAndTime(Long srId, Date startTime, Date endTime) {

        Calendar start = Calendar.getInstance();

        start.setTime(startTime);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MILLISECOND, 0);
        startTime = start.getTime();

        List<SpinReserveBid> spinReserveBidList = this.getCapacityRevenue(spinReserveService.findAllBySrIdAndTime(srId, startTime));

        List<SpinReserveBid> spinReserveBidList1 = this.getEfficacyRevenue(spinReserveBidList);

        List<SpinReserveBid> spinReserveBidList2 = this.getEnergyRevenue(spinReserveBidList1);

        if (spinReserveBidList2 != null && spinReserveBidList2.size() > 0) {
            //Save spinReserveBidList
            spinReserveService.addOrUpdateAll(srId, spinReserveBidList2);
        }

    }


    /***
     * 計算容量費用
     *
     * @param spinReserveBid
     * @return c 回填容量費用
     */
    public List<SpinReserveBid> getCapacityRevenue(List<SpinReserveBid> spinReserveBid) {

        for (SpinReserveBid data : spinReserveBid) {
            //取得SpinReserveBid 容量價格/得標容量/服務品質指標
            BigDecimal awardedCapacity = BigDecimal.ZERO;
            BigDecimal capacityPrice = BigDecimal.ZERO;
            BigDecimal serviceFactor = BigDecimal.ZERO;

            awardedCapacity = data.getAwarded_capacity();
            capacityPrice = data.getSr_price();
            serviceFactor = data.getServiceFactor();
            //設置效能費用初始為 0
            BigDecimal capacityRevenue = new BigDecimal("0");
            BigDecimal temp = new BigDecimal("0");
            if (awardedCapacity != null && capacityPrice != null && serviceFactor != null) {
                //MWh * $/MW *%/100
                temp = awardedCapacity.multiply(capacityPrice);
                capacityRevenue = temp.multiply(serviceFactor.divide(new BigDecimal(100), 3, BigDecimal.ROUND_HALF_UP));
            }
            //更新 ListCalculated物件
            data.setCapacityRevenue(capacityRevenue);
            List<SpinReserveBidDetail> list = data.getList();
            if (list != null) {
                for (SpinReserveBidDetail objectList : list) {
                    BigDecimal listAwardedCapacity = new BigDecimal("0");
                    BigDecimal listCapacityPrice = new BigDecimal("0");
                    BigDecimal listServiceFactor = new BigDecimal("0");
                    BigDecimal temp2 = new BigDecimal("0");
                    BigDecimal listCapacityRevenue = new BigDecimal("0");

                    listAwardedCapacity = objectList.getAwarded_capacity();
                    //場域的容量價格 以TXG的容量價格 進行容量費計算
                    listCapacityPrice = capacityPrice;
                    //場域的服務指標 以TXG的服務指標 進行容量費計算
                    listServiceFactor = serviceFactor;

                    if (listAwardedCapacity != null && listCapacityPrice != null && listServiceFactor != null) {
                        temp2 = listAwardedCapacity.multiply(listCapacityPrice);
                        listCapacityRevenue = temp2.multiply(listServiceFactor.divide(new BigDecimal(100), 3, BigDecimal.ROUND_HALF_UP));
                    }
                    //更新 List物件
                    objectList.setCapacityRevenue(listCapacityRevenue);
                }
            }
            //} //Sam 20210218
        }
        return spinReserveBid;
    }


    /***
     * 計算效能費用
     *
     * @param spinReserveBid
     * @return spinReserveBid 回填效能費用
     */
    public List<SpinReserveBid> getEfficacyRevenue(List<SpinReserveBid> spinReserveBid) {

        for (SpinReserveBid data : spinReserveBid) {
            //SpinReserveStatistics spinReserveStatistics = new SpinReserveStatistics();
            SpinReserveProfile spinReserveProfile = data.getSpinReserveProfile();

            BigDecimal efficacyRevenue = new BigDecimal("0");
            BigDecimal efficacyPrice = new BigDecimal("0");
            BigDecimal serviceFactor = new BigDecimal("0");
            BigDecimal awardedCapacity = new BigDecimal("0");
            BigDecimal temp = new BigDecimal("0");

            efficacyPrice = spinReserveProfile.getEfficacyPrice();
            serviceFactor = data.getServiceFactor();
            awardedCapacity = data.getAwarded_capacity();

            if (awardedCapacity != null && efficacyPrice != null && serviceFactor != null) {
                //MW*$/MW*%/100
                temp = awardedCapacity.multiply(efficacyPrice);
                efficacyRevenue = temp.multiply(serviceFactor.divide(new BigDecimal(100), 3, BigDecimal.ROUND_HALF_UP));

            }

            data.setEfficacyRevenue(efficacyRevenue);

            //spinReserveStatistics.setEfficacyRevenue(efficacyRevenue);
            List<SpinReserveBidDetail> fieldlist = data.getList();
            if (fieldlist != null) {
                for (SpinReserveBidDetail objectList : fieldlist) {

                    BigDecimal listEfficacyRevenue = new BigDecimal("0");
                    BigDecimal fieldAwardedCapacity = new BigDecimal("0");

                    fieldAwardedCapacity = objectList.getAwarded_capacity();
                    if (fieldAwardedCapacity != null && efficacyPrice != null && serviceFactor != null) {

                        //MW*$/MW*%/100
                        //場域的效能價格 以TXG的效能價格 進行效能費計算
                        temp = fieldAwardedCapacity.multiply(efficacyPrice);
                        //場域的服務指標 以TXG的服務指標 進行效能費計算
                        listEfficacyRevenue = temp.multiply(serviceFactor.divide(new BigDecimal(100), 3, BigDecimal.ROUND_HALF_UP));

                    }

                    objectList.setEfficacyRevenue(listEfficacyRevenue);
                }
            }
        }
        return spinReserveBid;
    }


    /***
     * 計算電能費用
     *
     * @param spinReserveBid
     * @return spinReserveBid 回填電能費用
     */
    public List<SpinReserveBid> getEnergyRevenue(List<SpinReserveBid> spinReserveBid) {

        for (SpinReserveBid data : spinReserveBid) {
            //SpinReserveStatistics spinReserveStatistics = new SpinReserveStatistics();
            SpinReserveProfile spinReserveProfile = data.getSpinReserveProfile();

            BigDecimal energyRevenue = new BigDecimal("0");
            BigDecimal energyPrice = new BigDecimal("0");
            BigDecimal serviceFactor = new BigDecimal("0");
            BigDecimal hourlyServiceEnergy = new BigDecimal("0");
            BigDecimal temp = new BigDecimal("0");

            energyPrice = data.getEnergyPrice();
            serviceFactor = data.getServiceFactor();
            hourlyServiceEnergy = data.getServiceEnergy();

            if (hourlyServiceEnergy != null && energyPrice != null && serviceFactor != null) {
                //kWh/1000 * $/MW *%/100
                temp = hourlyServiceEnergy.multiply(energyPrice.divide(new BigDecimal(1000), 3, BigDecimal.ROUND_HALF_UP));
                energyRevenue = temp.multiply(serviceFactor.divide(new BigDecimal(100), 3, BigDecimal.ROUND_HALF_UP));

            }

            data.setEnergyRevenue(energyRevenue);

            List<SpinReserveBidDetail> fieldlist = data.getList();
            if (fieldlist != null) {
                for (SpinReserveBidDetail objectList : fieldlist) {

                    BigDecimal listEnergyRevenue = new BigDecimal("0");
                    BigDecimal fieldHourlyServiceEnergy = objectList.getServiceEnergy();

                    if (fieldHourlyServiceEnergy != null && energyPrice != null && serviceFactor != null) {

                        //kWh/1000 * $/MW *%/100
                        //場域的電能價格 以TXG的電能價格 進行電能費計算
                        temp = fieldHourlyServiceEnergy.multiply(energyPrice.divide(new BigDecimal(1000), 3, BigDecimal.ROUND_HALF_UP));
                        //場域的服務指標 以TXG的服務指標 進行效能費計算
                        listEnergyRevenue = temp.multiply(serviceFactor.divide(new BigDecimal(100), 3, BigDecimal.ROUND_HALF_UP));

                    }

                    objectList.setEnergyRevenue(listEnergyRevenue);

                }
            }
        }
        return spinReserveBid;
    }


    /***
     * serviceEnergy與 performanceEnergy計算
     *
     * @param spinReserveData
     * @return spinReserveData ；回填serviceEnergy(kWh)服務期間提供電量 與 performanceEnergy(kWh)執行率期間提供電量
     */
    public List<SpinReserveData> getEnergy(Long srId, List<SpinReserveData> spinReserveDataList, AncillaryServiceType serviceType) {

        Date timestampStart = new Date();    //1582819200000L
        Date timestampEnd = new Date();    //1582819200000L
        Calendar tempTime = Calendar.getInstance();

        Date t1 = new Date(); //通知時間
        Date t2 = new Date(); //服務開始間
        Date t3 = new Date(); //執行率計算截止時間(t2+服務規定時間)
        Date t4 = new Date(); //服務結束時間
        Date t5 = new Date(); //恢復截止時間(t4+恢復規定時間)

        BigDecimal t51 = BigDecimal.ZERO; //(t5-t1)

        BigDecimal txgEnergyMeterT1 = BigDecimal.ZERO;
        BigDecimal txgEnergyMeterT2 = BigDecimal.ZERO;
        BigDecimal txgEnergyMeterT3 = BigDecimal.ZERO;
        BigDecimal txgEnergyMeterT4 = BigDecimal.ZERO;
        BigDecimal txgEnergyMeterT5 = BigDecimal.ZERO;

        BigDecimal fieldPerformanceEnergy = BigDecimal.ZERO;
        BigDecimal fieldServiceEnergy = BigDecimal.ZERO;

        BigDecimal txgPerformanceEnergy = BigDecimal.ZERO;
        BigDecimal txgServiceEnergy = BigDecimal.ZERO;
        BigDecimal txgEnergyCBLT32 = BigDecimal.ZERO;
        BigDecimal txgEnergyCBLT51 = BigDecimal.ZERO;
        BigDecimal txgAccFactor = BigDecimal.ONE;

        for (SpinReserveData txgData : spinReserveDataList) {

            txgData.setSpinReserveProfile(new SpinReserveProfile(srId));//要把srId加回去

            timestampStart = txgData.getStartTime();
            timestampEnd = txgData.getEndTime();

            t1 = txgData.getNoticeTime();
            t2 = txgData.getStartTime();
            t4 = txgData.getEndTime();

            //台電 t2 & t4 會給整分的資料，程式再加秒數清除，預防非整分資料
            tempTime.setTime(t2);
            tempTime.set(Calendar.SECOND, 0);
            t2 = tempTime.getTime();

            tempTime.setTime(t4);
            tempTime.set(Calendar.SECOND, 0);
            t4 = tempTime.getTime();

            //計算t3 與 t5

            switch (serviceType) {
                case sr:
                    tempTime.setTime(t2);
                    tempTime.add(Calendar.MINUTE, -10); //減10分鐘
                    t1 = tempTime.getTime();

                    t3 = t4;

                    tempTime.setTime(t4);
                    tempTime.add(Calendar.MINUTE, 10); //加10分鐘
                    t5 = tempTime.getTime();

                    break;
                case sup:
                    tempTime.setTime(t2);
                    tempTime.add(Calendar.MINUTE, -30); //減30分鐘
                    t1 = tempTime.getTime();

                    tempTime.setTime(t2);
                    tempTime.add(Calendar.HOUR, 2); //加2小時
                    t3 = tempTime.getTime();

                    tempTime.setTime(t4);
                    tempTime.add(Calendar.MINUTE, 30); //加30分鐘
                    t5 = tempTime.getTime();

                    break;

                default:

                    break;

            }

            t51 = new BigDecimal((t5.getTime() - t1.getTime())).divide(new BigDecimal(3600000), 3, BigDecimal.ROUND_HALF_UP);

            //取得TXG包含的場域
            List<SpinReserveDataDetail> detailList = txgData.getList();

            //計算field serviceEnergy & performanceEnergy         
            for (SpinReserveDataDetail fieldList : detailList) {

                //取得場域用電資訊
                Long fieldId = fieldList.getFieldProfile().getId();
                BigDecimal fieldAccFactor = fieldList.getFieldProfile().getAccFactor();
                if (fieldAccFactor == null) {
                    fieldAccFactor = new BigDecimal(1);
                }

                //尚未處理單一電表~多電表 overflow問題
                Optional<ElectricData> ed1 = statisticsService.findElectricData(fieldId, DataType.T99, t1);
                Optional<ElectricData> ed2 = statisticsService.findElectricData(fieldId, DataType.T99, t2);
                Optional<ElectricData> ed3 = statisticsService.findElectricData(fieldId, DataType.T99, t3);
                Optional<ElectricData> ed4 = statisticsService.findElectricData(fieldId, DataType.T99, t4);
                Optional<ElectricData> ed5 = statisticsService.findElectricData(fieldId, DataType.T99, t5);

                BigDecimal fieldEnergyMeterT1 = ed1.get().getTotalkWh();
                BigDecimal fieldEnergyMeterT2 = ed2.get().getTotalkWh();
                BigDecimal fieldEnergyMeterT3 = ed3.get().getTotalkWh();
                BigDecimal fieldEnergyMeterT4 = ed4.get().getTotalkWh();
                BigDecimal fieldEnergyMeterT5 = ed5.get().getTotalkWh();

                fieldPerformanceEnergy = BigDecimal.ZERO;
                fieldServiceEnergy = BigDecimal.ZERO;

                BigDecimal fieldEnergyCBLT32 = BigDecimal.ZERO;
                BigDecimal fieldEnergyCBLT51 = BigDecimal.ZERO;


                switch (serviceType) {
                    case sr:
                        //用CBL(baseLine)估算 energyCBL
                        fieldEnergyCBLT32 = fieldList.getBaseline();//.multiply(new BigDecimal(1)).setScale(1, BigDecimal.ROUND_HALF_UP);
                        fieldEnergyCBLT51 = fieldList.getBaseline().multiply(t51).setScale(1, BigDecimal.ROUND_HALF_UP);
                        break;
                    case sup:
                        //用CBL(baseLine)估算 energyCBL
                        fieldEnergyCBLT32 = fieldList.getBaseline().multiply(new BigDecimal(2)).setScale(1, BigDecimal.ROUND_HALF_UP);
                        fieldEnergyCBLT51 = fieldList.getBaseline().multiply(t51).setScale(1, BigDecimal.ROUND_HALF_UP);
                        break;

                    default:

                        break;

                }

                //fieldEnergyCBLT32 -(fieldEnergyMeterT3-fieldEnergyMeterT2)*accFactor (kWh)
                fieldPerformanceEnergy = fieldEnergyCBLT32.subtract(fieldAccFactor.multiply(
                        fieldEnergyMeterT3.subtract(fieldEnergyMeterT2)).setScale(1, BigDecimal.ROUND_HALF_UP));

                //fieldEnergyCBLT51 -(fieldEnergyMeterT5-fieldEnergyMeterT1)*accFactor (kWh)
                fieldServiceEnergy = fieldEnergyCBLT51.subtract(fieldAccFactor.multiply(
                        fieldEnergyMeterT5.subtract(fieldEnergyMeterT1)).setScale(1, BigDecimal.ROUND_HALF_UP));

                fieldList.setPerformanceEnergy(fieldPerformanceEnergy);
                fieldList.setServiceEnergy(fieldServiceEnergy);

                //累加TXG T1~T5 各時間點的累積用電量 (尚未處裡overflow問題)
                txgEnergyMeterT1 = txgEnergyMeterT1.add(fieldEnergyMeterT1);
                txgEnergyMeterT2 = txgEnergyMeterT2.add(fieldEnergyMeterT2);
                txgEnergyMeterT3 = txgEnergyMeterT3.add(fieldEnergyMeterT3);
                txgEnergyMeterT4 = txgEnergyMeterT4.add(fieldEnergyMeterT4);
                txgEnergyMeterT5 = txgEnergyMeterT5.add(fieldEnergyMeterT5);

            }

            txgEnergyCBLT32 = BigDecimal.ZERO;
            txgEnergyCBLT51 = BigDecimal.ZERO;

            switch (serviceType) {
                case sr:
                    //用CBL(baseLine)估算 energyCBL
                    txgEnergyCBLT32 = txgData.getBaseline();//.multiply(new BigDecimal(1)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    txgEnergyCBLT51 = txgData.getBaseline().multiply(t51).setScale(1, BigDecimal.ROUND_HALF_UP);
                    break;
                case sup:
                    //用CBL(baseLine)估算 energyCBL
                    txgEnergyCBLT32 = txgData.getBaseline().multiply(new BigDecimal(2)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    txgEnergyCBLT51 = txgData.getBaseline().multiply(t51).setScale(1, BigDecimal.ROUND_HALF_UP);
                    break;

                default:

                    break;

            }

            //取得誤差因子，以第一個場域為代表，之後要改為TXG的  //Sam 20210826
            txgAccFactor = txgData.getList().get(0).getFieldProfile().getAccFactor();
            if (txgAccFactor == null) {
                txgAccFactor = new BigDecimal(1);
            }

            //txgEnergyCBLT32 -(txgEnergyMeterT3-txgEnergyMeterT2)*txgAccFactor (kWh)
            txgPerformanceEnergy = txgEnergyCBLT32.subtract(txgAccFactor.multiply(
                    txgEnergyMeterT3.subtract(txgEnergyMeterT2)).setScale(1, BigDecimal.ROUND_HALF_UP));

            //txgEnergyCBLT51 -(txgEnergyMeterT5-txgEnergyMeterT1)*txgAccFactor (kWh)
            txgServiceEnergy = txgEnergyCBLT51.subtract(txgAccFactor.multiply(
                    txgEnergyMeterT5.subtract(txgEnergyMeterT1)).setScale(1, BigDecimal.ROUND_HALF_UP));

            txgData.setPerformanceEnergy(txgPerformanceEnergy);
            txgData.setServiceEnergy(txgServiceEnergy);

        }

        return spinReserveDataList;
    }


    /***
     * 調度期間每小時serviceEnergy計算
     *
     * @param spinReserveBid 與 spinReserveData 與 服務類型
     * @return spinReserveBid ；回填hourlyServiceEnergy(kWh)
     */
    public List<SpinReserveBid> getHourlyServiceEnergy(Long srId, List<SpinReserveData> spinReserveDataList, AncillaryServiceType serviceType) {

        List<SpinReserveBid> spinReserveBidList = null;

        Date timestampStart = new Date();    //1582819200000L
        Date timestampEnd = new Date();    //1582819200000L
        Calendar tempTime = Calendar.getInstance();

        Date t1 = new Date(); //通知時間
        Date t2 = new Date(); //服務開始間
        Date t3 = new Date(); //執行率計算截止時間(t2+服務規定時間)
        Date t4 = new Date(); //服務結束時間
        Date t5 = new Date(); //恢復截止時間(t4+恢復規定時間)

        List<Date> timeSeries = new ArrayList<>();
        List<BigDecimal> txgEnergyTimeSeries = new ArrayList<>();
        List<ArrayList<BigDecimal>> fieldEnergyTimeSeries = new ArrayList<>();
        BigDecimal txgEnergy = BigDecimal.ZERO;
        BigDecimal txgAccFactor = BigDecimal.ONE;
        BigDecimal txgEnergyCBL = BigDecimal.ZERO;


        for (SpinReserveData txgData : spinReserveDataList) {

            txgData.setSpinReserveProfile(new SpinReserveProfile(srId));//要把srId加回去

            timestampStart = txgData.getStartTime();
            timestampEnd = txgData.getEndTime();

            t1 = txgData.getNoticeTime();
            t2 = txgData.getStartTime();
            t4 = txgData.getEndTime();

            //台電 t2 & t4 會給整分的資料，程式再加秒數清除，預防非整分資料
            tempTime.setTime(t2);
            tempTime.set(Calendar.SECOND, 0);
            t2 = tempTime.getTime();

            tempTime.setTime(t4);
            tempTime.set(Calendar.SECOND, 0);
            t4 = tempTime.getTime();

            //計算t3 與 t5

            switch (serviceType) {
                case sr:
                    tempTime.setTime(t2);
                    tempTime.add(Calendar.MINUTE, -10); //減10分鐘
                    t1 = tempTime.getTime();

                    t3 = t4;

                    tempTime.setTime(t4);
                    tempTime.add(Calendar.MINUTE, 10); //加10分鐘
                    t5 = tempTime.getTime();

                    break;
                case sup:
                    tempTime.setTime(t2);
                    tempTime.add(Calendar.MINUTE, -30); //減30分鐘
                    t1 = tempTime.getTime();

                    tempTime.setTime(t2);
                    tempTime.add(Calendar.HOUR, 2); //加2小時
                    t3 = tempTime.getTime();

                    tempTime.setTime(t4);
                    tempTime.add(Calendar.MINUTE, 30); //加30分鐘
                    t5 = tempTime.getTime();

                    break;

                default:

                    break;

            }

            Integer maxCount = 10;
            for (Integer i = 0; i < maxCount; i++) {

                if (i.equals(0)) {

                    timeSeries.add(t1);

                } else {

                    tempTime.setTime(t1);
                    tempTime.set(Calendar.SECOND, 0);
                    tempTime.set(Calendar.MINUTE, 0);
                    tempTime.add(Calendar.HOUR, i);

                    if (tempTime.getTime().before(t5)) { //小於T5

                        timeSeries.add(tempTime.getTime());

                    } else {

                        timeSeries.add(t5);
                        break;
                    }

                }

            }

            //取得TXG包含的場域
            List<SpinReserveDataDetail> detailList = txgData.getList();

            //計算TXG各時間點的累積用電度數
            for (Date time : timeSeries) {

                txgEnergy = BigDecimal.ZERO;
                List<BigDecimal> tempFieldEnergyTimeSeries = new ArrayList<>();

                //計算field serviceEnergy & performanceEnergy
                for (SpinReserveDataDetail fieldList : detailList) {

                    //取得場域用電資訊
                    Long fieldId = fieldList.getFieldProfile().getId();

                    //尚未處理單一電表~多電表 overflow問題
                    Optional<ElectricData> ed = statisticsService.findElectricData(fieldId, DataType.T99, time);
                    BigDecimal fieldEnergyMeter = ed.get().getTotalkWh();

                    txgEnergy = txgEnergy.add(fieldEnergyMeter);
                    tempFieldEnergyTimeSeries.add(fieldEnergyMeter);
                }

                txgEnergyTimeSeries.add(txgEnergy);
                fieldEnergyTimeSeries.add((ArrayList<BigDecimal>) tempFieldEnergyTimeSeries);
            }

            tempTime.setTime(timeSeries.get(0));
            tempTime.set(Calendar.SECOND, 0);
            tempTime.set(Calendar.MINUTE, 0);
            timestampStart = tempTime.getTime();

            tempTime.setTime(timeSeries.get(timeSeries.size() - 1));
            tempTime.set(Calendar.SECOND, 0);
            tempTime.set(Calendar.MINUTE, 0);
            tempTime.add(Calendar.HOUR, 1);
            timestampEnd = tempTime.getTime();

            spinReserveBidList = spinReserveService.findAllBySrIdAndTime(srId, timestampStart, timestampEnd);
            //計算各小時的電能量
            for (int i = 0; i < (timeSeries.size() - 1); i++) {

                BigDecimal duration = new BigDecimal((timeSeries.get(i + 1).getTime() - timeSeries.get(i).getTime()))
                        .divide(new BigDecimal(3600000), 3, BigDecimal.ROUND_HALF_UP);

                txgEnergyCBL = txgData.getBaseline().multiply(duration).setScale(1, BigDecimal.ROUND_HALF_UP);
                //取得誤差因子，以第一個場域為代表，之後要改為TXG的  //Sam 20210826
                txgAccFactor = txgData.getList().get(0).getFieldProfile().getAccFactor();
                if (txgAccFactor == null) {
                    txgAccFactor = new BigDecimal(1);
                }

                spinReserveBidList.get(i).setServiceEnergy(txgEnergyCBL.subtract(txgAccFactor.multiply(
                        txgEnergyTimeSeries.get(i + 1).subtract(txgEnergyTimeSeries.get(i))).setScale(1, BigDecimal.ROUND_HALF_UP)));


                for (int y = 0; y < spinReserveBidList.get(i).getList().size(); y++) {

                    BigDecimal fieldEnergyCBL = BigDecimal.ZERO;
                    BigDecimal fieldAccFactor = txgData.getList().get(y).getFieldProfile().getAccFactor();
                    if (fieldAccFactor == null) {
                        fieldAccFactor = new BigDecimal(1);
                    }

                    fieldEnergyCBL = txgData.getList().get(y).getBaseline().multiply(duration).setScale(1, BigDecimal.ROUND_HALF_UP);

                    spinReserveBidList.get(i).getList().get(y).setServiceEnergy(fieldEnergyCBL.subtract(fieldAccFactor.multiply(
                            fieldEnergyTimeSeries.get(i + 1).get(y).subtract(fieldEnergyTimeSeries.get(i).get(y))).setScale(1, BigDecimal.ROUND_HALF_UP)));

                }

            }

        }

        return spinReserveBidList;
    }


    /***
     * 調度通知之該小時serviceFactor計算
     *
     * @param spinReserveBid 與 spinReserveData 與 服務類型
     * @return spinReserveBid ；回填serviceFactor
     */
    public List<SpinReserveBid> getServiceFactor(Long srId, List<SpinReserveData> spinReserveDataList, AncillaryServiceType serviceType) {

        List<SpinReserveBid> spinReserveBidList = null;
        Date timestampStart = new Date();    //1582819200000L
        Date timestampEnd = new Date();    //1582819200000L
        Calendar tempTime = Calendar.getInstance();
        Date t1 = new Date(); //通知時間
        Date t2 = new Date(); //服務開始間
        BigDecimal serviceFactor = BigDecimal.ZERO;
        BigDecimal txgRevenueFactor = BigDecimal.ZERO;

        for (SpinReserveData txgData : spinReserveDataList) {

            txgData.setSpinReserveProfile(new SpinReserveProfile(srId));//要把srId加回去

            t1 = txgData.getNoticeTime();
            t2 = txgData.getStartTime();

            //台電 t2 會給整分的資料，程式再加秒數清除，預防非整分資料
            tempTime.setTime(t2);
            tempTime.set(Calendar.SECOND, 0);
            t2 = tempTime.getTime();

            //由t2回推t1，DNP傳來的t1不一定會是整分；但台電計算會已開始時間進行計算
            switch (serviceType) {
                case sr:
                    tempTime.setTime(t2);
                    tempTime.add(Calendar.MINUTE, -10); //減10分鐘
                    t1 = tempTime.getTime();
                    break;

                case sup:
                    tempTime.setTime(t2);
                    tempTime.add(Calendar.MINUTE, -30); //減30分鐘
                    t1 = tempTime.getTime();
                    break;

                default:
                    break;

            }

            tempTime.setTime(t1);
            tempTime.set(Calendar.MINUTE, 0); //分鐘清0
            tempTime.set(Calendar.SECOND, 0); //秒清0
            tempTime.set(Calendar.MILLISECOND, 0); //微秒清0
            timestampStart = tempTime.getTime();

            tempTime.add(Calendar.HOUR, 1); //加1小時
            timestampEnd = tempTime.getTime();

            txgRevenueFactor = txgData.getRevenueFactor();

            switch (serviceType) {
                case sr:
                    if (txgData.getRevenueFactor().compareTo(new BigDecimal(95)) >= 0) {

                        serviceFactor = new BigDecimal(100);

                    } else if (txgRevenueFactor.compareTo(new BigDecimal(95)) < 0 && txgRevenueFactor.compareTo(new BigDecimal(85)) >= 0) {

                        serviceFactor = new BigDecimal(70);

                    } else if (txgRevenueFactor.compareTo(new BigDecimal(85)) < 0 && txgRevenueFactor.compareTo(new BigDecimal(70)) >= 0) {

                        serviceFactor = new BigDecimal(0);

                    } else {

                        serviceFactor = new BigDecimal(-240);

                    }
                    break;

                case sup:
                    if (txgData.getRevenueFactor().compareTo(new BigDecimal(95)) >= 0) {

                        serviceFactor = new BigDecimal(100);

                    } else if (txgRevenueFactor.compareTo(new BigDecimal(95)) < 0 && txgRevenueFactor.compareTo(new BigDecimal(85)) >= 0) {

                        serviceFactor = new BigDecimal(70);

                    } else if (txgRevenueFactor.compareTo(new BigDecimal(85)) < 0 && txgRevenueFactor.compareTo(new BigDecimal(70)) >= 0) {

                        serviceFactor = new BigDecimal(0);

                    } else {

                        serviceFactor = new BigDecimal(-24);

                    }
                    break;

                default:
                    break;

            }

            spinReserveBidList = spinReserveService.findAllBySrIdAndTime(srId, timestampStart, timestampEnd);
            spinReserveBidList.get(0).setServiceFactor(serviceFactor);
        }

        return spinReserveBidList;
    }


    /***
     * performance計算
     *
     * @param spinReserveData 與 服務類型
     * @return spinReserveData ；回填performance (revenueFactor)每分鐘平均執行率
     */
    public List<SpinReserveData> getProformance(Long srId, List<SpinReserveData> spinReserveDataList, AncillaryServiceType serviceType) {

        Date timestampStart = new Date();    //1582819200000L
        Date timestampEnd = new Date();    //1582819200000L
        Calendar tempTime = Calendar.getInstance();

        Date t1 = new Date(); //通知時間
        Date t2 = new Date(); //服務開始間
        Date t3 = new Date(); //執行率計算截止時間(t2+服務規定時間)
        Date t4 = new Date(); //服務結束時間
        Date t5 = new Date(); //恢復截止時間(t4+恢復規定時間)

        BigDecimal t51 = BigDecimal.ZERO; //(t5-t1)

        BigDecimal fieldPerformanceEnergy = BigDecimal.ZERO;
        BigDecimal fieldRevenueFactor = BigDecimal.ZERO;
        BigDecimal fieldClipKW = BigDecimal.ZERO;

        BigDecimal txgPerformanceEnergy = BigDecimal.ZERO;
        BigDecimal txgRevenueFactor = BigDecimal.ZERO;
        BigDecimal txgClipKW = BigDecimal.ZERO;

        for (SpinReserveData txgData : spinReserveDataList) {

            txgData.setSpinReserveProfile(new SpinReserveProfile(srId));//要把srId加回去

            //取得TXG包含的場域
            List<SpinReserveDataDetail> detailList = txgData.getList();

            //計算field serviceEnergy & performanceEnergy         
            for (SpinReserveDataDetail fieldList : detailList) {

                //取得場域performanceEnergy 與 clipkW	
                fieldPerformanceEnergy = fieldList.getPerformanceEnergy();
                fieldClipKW = fieldList.getClipKW(); //field的 clipkW是BigDecimal


                switch (serviceType) {
                    case sr:

                        fieldRevenueFactor = fieldPerformanceEnergy.divide(fieldClipKW.multiply(new BigDecimal(1)), 3, BigDecimal.ROUND_HALF_UP);
                        break;
                    case sup:

                        fieldRevenueFactor = fieldPerformanceEnergy.divide(fieldClipKW.multiply(new BigDecimal(2)), 3, BigDecimal.ROUND_HALF_UP);
                        break;

                    default:

                        break;

                }
                //setScale(0, BigDecimal.ROUND_HALF_UP --> 四捨五入小數點第0位
                fieldRevenueFactor = fieldRevenueFactor.multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP);
                fieldList.setRevenueFactor(fieldRevenueFactor);

            }

            //取得TXG performanceEnergy 與 clipkW	
            txgPerformanceEnergy = txgData.getPerformanceEnergy();
            txgClipKW = new BigDecimal(txgData.getClipKW()); //TXG的 clipkW是Integer


            switch (serviceType) {
                case sr:

                    txgRevenueFactor = txgPerformanceEnergy.divide(txgClipKW.multiply(new BigDecimal(1)), 3, BigDecimal.ROUND_HALF_UP);
                    break;
                case sup:

                    txgRevenueFactor = txgPerformanceEnergy.divide(txgClipKW.multiply(new BigDecimal(2)), 3, BigDecimal.ROUND_HALF_UP);
                    break;

                default:

                    break;

            }

            txgRevenueFactor = txgRevenueFactor.multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP);
            txgData.setRevenueFactor(txgRevenueFactor);

        }

        return spinReserveDataList;

    }


    /**
     * 績效計算部分
     */

    /***
     * 所有TXG績效統計(包含 參數計算 與 revenue計算)
     *
     * @param timestampStart
     * @param timestampEnd
     *
     *
     */
    public void spinReserveRevenueStatisticsByTime(Date timestampStart, Date timestampEnd, AncillaryServiceType serviceType) {

        List<SpinReserveProfile> list = spinReserveService.findEnableSpinReserveProfile();

        for (SpinReserveProfile spinReserveProfile : list) {

            this.spinReserveRevenueStatisticsBySrIdAndTime(spinReserveProfile.getId(), timestampStart, timestampEnd, serviceType);
        }

    }


    /***
     * 單一TXG績效統計 (包含 參數計算 與 revenue計算)
     *
     * @param timestampStart
     * @param timestampEnd
     *
     *
     */
    public void spinReserveRevenueStatisticsBySrIdAndTime(Long srId, Date timestampStart, Date timestampEnd, AncillaryServiceType serviceType) {

        Date startTime = timestampStart;
        Date endTime = timestampEnd;

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        Calendar next = Calendar.getInstance();

        if (timestampStart.before(timestampEnd)) {

            //參數(重新)計算
            this.serviceParametersCalculate(srId, startTime, endTime, serviceType);

            //revenue(重新)計算
            this.revenueCalculateBySridAndTime(srId, startTime, endTime);

            startTime = timestampStart;
            endTime = timestampEnd;

            start = Calendar.getInstance();
            end = Calendar.getInstance();
            next = Calendar.getInstance();

            start.setTime(timestampStart);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MILLISECOND, 0);
            startTime = start.getTime();

            end.setTime(timestampEnd);
            end.set(Calendar.MINUTE, 0);
            end.set(Calendar.SECOND, 0);
            end.set(Calendar.HOUR_OF_DAY, 0);
            end.set(Calendar.MILLISECOND, 0);
            endTime = end.getTime();

            //統計該時段每日資料
            long diffInMillies = Math.abs(endTime.getTime() - startTime.getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            if (diff == 0) {
                diff = 1;
            }

            for (int i = 0; i < diff; i++) {

                next.setTime(startTime);
                next.add(Calendar.DATE, 1);
                Date nextTime = next.getTime();

                this.dailyRevenueStatisticsByIdAndTimeInDay(srId, startTime);

                //換下一日
                start.add(Calendar.DATE, 1);
                startTime = start.getTime();

            }//for(diff)

            //統計該時段每月資料
            start.setTime(timestampStart);
            start.set(Calendar.DAY_OF_MONTH, 1);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.HOUR_OF_DAY, 0);
            startTime = start.getTime();

            end.setTime(timestampEnd);
            end.set(Calendar.DAY_OF_MONTH, 1);
            end.set(Calendar.MINUTE, 0);
            end.set(Calendar.SECOND, 0);
            end.set(Calendar.HOUR_OF_DAY, 0);
            endTime = end.getTime();

            //統計該時段每日資料
            int startYear = start.get(Calendar.YEAR);
            int startMonth = start.get(Calendar.MONTH) + 1;

            int endYear = end.get(Calendar.YEAR);
            int endMonth = end.get(Calendar.MONTH) + 1;

            int diffInMonths = (endYear - startYear) * 12 + (endMonth - startMonth) + 1;

            for (int i = 0; i < diffInMonths; i++) {

                this.monthlyRevenueStatisticsByIdAndTimeInMonth(srId, startTime);

                start.add(Calendar.MONTH, 1);

            }//for(diffInMonths)

        } //if (timestampStart.before(timestampEnd))

    }


    /***
     * 日績效統計 
     *
     * @param srId      SrID
     * @param startTime 起始時間(取該日00:00)
     *
     */
    public void dailyRevenueStatisticsByIdAndTimeInDay(Long srId, Date timestampStart) {

        Date startTime = timestampStart;
        Date endTime = timestampStart;
        Calendar tempCalender = Calendar.getInstance();

        List<SpinReserveBid> spinReserveBidInDayList = null;
        BigDecimal capacityRevenueInDay = new BigDecimal("0");
        BigDecimal energyRevenueInDay = new BigDecimal("0");
        BigDecimal efficacyRevenueInDay = new BigDecimal("0");
        Integer awardedCountInDay = 0;
        BigDecimal awardedCapacityInDay = new BigDecimal("0");

        BigDecimal avgRevenueFactorInDay = new BigDecimal("0");
        BigDecimal avgCapacityPriceInDay = new BigDecimal("0");
        BigDecimal totalAwardedCapacityInDay = new BigDecimal("0");

        tempCalender.setTime(timestampStart);
        tempCalender.set(Calendar.MINUTE, 0);
        tempCalender.set(Calendar.SECOND, 0);
        tempCalender.set(Calendar.HOUR_OF_DAY, 0);
        tempCalender.set(Calendar.MILLISECOND, 0);
        startTime = tempCalender.getTime();

        tempCalender.add(Calendar.DAY_OF_MONTH, 1);
        endTime = tempCalender.getTime();

        Optional<SpinReserveStatistics> spinReserveStatistics =
                spinReserveService.findSpinReserveStatistics(srId, StatisticsType.day, startTime);

        SpinReserveStatistics newSpinReserveStatistics;

        if (spinReserveStatistics.isPresent()) {
            newSpinReserveStatistics = spinReserveStatistics.get();
        } else {
            newSpinReserveStatistics = new SpinReserveStatistics();
        }

        newSpinReserveStatistics.setSpinReserveProfile(new SpinReserveProfile(srId));
        //強制更新時間為每日00:00(上午12:00)
        newSpinReserveStatistics.setTime(startTime);
        Date updateTime = new Date();
        newSpinReserveStatistics.setUpdateTime(updateTime);
        newSpinReserveStatistics.setStatisticsType(StatisticsType.day);

        spinReserveBidInDayList = spinReserveService.findAllBySrIdAndTime(srId, startTime, endTime);

        List<SpinReserveStatisticsDetail> fieldStatisticsDetailList = new ArrayList<>();

        for (SpinReserveBid srHourlyBid : spinReserveBidInDayList) {

            if ((srHourlyBid.getCapacityRevenue() != null) && (srHourlyBid.getCapacityRevenue().compareTo(BigDecimal.ZERO) > 0)) {

                capacityRevenueInDay = capacityRevenueInDay.add(srHourlyBid.getCapacityRevenue());
                energyRevenueInDay = energyRevenueInDay.add(srHourlyBid.getEnergyRevenue());
                efficacyRevenueInDay = efficacyRevenueInDay.add(srHourlyBid.getEfficacyRevenue());
                awardedCountInDay = awardedCountInDay + 1;
                totalAwardedCapacityInDay = totalAwardedCapacityInDay.add(srHourlyBid.getAwarded_capacity());

            }

            //field資料
            for (SpinReserveBidDetail fieldBid : srHourlyBid.getList()) {

                //List<SpinReserveStatisticsDetail> fieldStatisticsDetailList = newSpinReserveStatistics.getList();
                Boolean fieldIdStatus = true;

                if (fieldStatisticsDetailList != null) {

                    for (SpinReserveStatisticsDetail fieldStatistics : fieldStatisticsDetailList) {

                        if (fieldBid.getFieldProfile().getId() == fieldStatistics.getFieldProfile().getId()) {
                            fieldIdStatus = false;

                            fieldStatistics.setUpdateTime(new Date());

                            if (fieldBid.getCapacityRevenue() != null) {
                                fieldStatistics.setCapacityRevenue(fieldStatistics.getCapacityRevenue().add(fieldBid.getCapacityRevenue()));        //容量費
                            }

                            if (fieldBid.getEnergyRevenue() != null) {
                                fieldStatistics.setEnergyRevenue(fieldStatistics.getEnergyRevenue().add(fieldBid.getEnergyRevenue()));            //電能費(新)
                                fieldStatistics.setEnergyPrice(fieldStatistics.getEnergyPrice().add(fieldBid.getEnergyRevenue()));                //電能費(舊)
                            }

                            if (fieldBid.getEfficacyRevenue() != null) {
                                fieldStatistics.setEfficacyRevenue(fieldStatistics.getEfficacyRevenue().add(fieldBid.getEfficacyRevenue()));        //效能費
                            }

                            if ((fieldBid.getCapacityRevenue() != null) && (fieldBid.getCapacityRevenue().compareTo(BigDecimal.ZERO) > 0)) {
                                fieldStatistics.setAwardedCount(fieldStatistics.getAwardedCount() + 1);            //得標時數
                            }


                            if (fieldBid.getAwarded_capacity() != null) {
                                fieldStatistics.setTotalAwardedCapacity(fieldStatistics.getTotalAwardedCapacity().add(fieldBid.getAwarded_capacity()));    //總得標量
                            }


                        }

                    }

                }// if(!=null)

                if (fieldIdStatus) {
                    //增加Detail資料
                    SpinReserveStatisticsDetail obj = new SpinReserveStatisticsDetail();
                    obj.setFieldProfile(new FieldProfile(fieldBid.getFieldProfile().getId()));
                    obj.setUpdateTime(new Date());
                    obj.setCreateTime(new Date());

                    if (fieldBid.getCapacityRevenue() != null) {

                        obj.setCapacityRevenue(fieldBid.getCapacityRevenue());

                    } else {

                        obj.setCapacityRevenue(BigDecimal.ZERO);
                    }

                    if (fieldBid.getEnergyRevenue() != null) {

                        obj.setEnergyPrice(fieldBid.getEnergyRevenue());
                        obj.setEnergyRevenue(fieldBid.getEnergyRevenue());

                    } else {

                        obj.setEnergyPrice(BigDecimal.ZERO);
                        obj.setEnergyRevenue(BigDecimal.ZERO);
                    }

                    if (fieldBid.getEfficacyRevenue() != null) {

                        obj.setEfficacyRevenue(fieldBid.getEfficacyRevenue());

                    } else {

                        obj.setEfficacyRevenue(BigDecimal.ZERO);
                    }


                    if ((fieldBid.getCapacityRevenue() != null) && (fieldBid.getCapacityRevenue().compareTo(BigDecimal.ZERO) > 0)) {

                        obj.setAwardedCount(new Integer(1));

                    } else {

                        obj.setAwardedCount(new Integer(0));

                    }


                    if (fieldBid.getAwarded_capacity() != null) {

                        obj.setTotalAwardedCapacity(fieldBid.getAwarded_capacity());

                    } else {

                        obj.setTotalAwardedCapacity(BigDecimal.ZERO);
                    }

                    obj.setAvgCapacityPrice(new BigDecimal("0"));
                    obj.setAvgRevenueFactor(new BigDecimal("0"));
                    obj.setNoticeConut(new Integer(0));

                    fieldStatisticsDetailList.add(obj);

                }

            }//for(SpinReserveBidDetail fieldBid)

        }  //for(srHourlyBid)

        //Sam 20201103
        if ((capacityRevenueInDay.compareTo(BigDecimal.ZERO) > 0) && (capacityRevenueInDay != null) && (totalAwardedCapacityInDay.compareTo(BigDecimal.ZERO) > 0)) {
            avgCapacityPriceInDay = capacityRevenueInDay.divide(totalAwardedCapacityInDay, 0, BigDecimal.ROUND_HALF_UP);
        }

        for (SpinReserveStatisticsDetail fieldStatistics : fieldStatisticsDetailList) {

            if ((fieldStatistics.getCapacityRevenue().compareTo(BigDecimal.ZERO) > 0) &&
                    (fieldStatistics.getCapacityRevenue() != null) &&
                    (fieldStatistics.getTotalAwardedCapacity().compareTo(BigDecimal.ZERO) > 0)) {
                //廠域平均得標價格
                fieldStatistics.setAvgCapacityPrice(fieldStatistics.getCapacityRevenue().divide(fieldStatistics.getTotalAwardedCapacity(), 1, BigDecimal.ROUND_HALF_UP));
            }
        }

        newSpinReserveStatistics.setList(fieldStatisticsDetailList);

        newSpinReserveStatistics.setCapacityRevenue(capacityRevenueInDay);                        //容量費

        //目前在spinReserveStatistics中， energyPrice 與 energyRevenue都是電能費，之後會統一用energyRevenue
        newSpinReserveStatistics.setEnergyPrice(energyRevenueInDay);                            //電能費
        newSpinReserveStatistics.setEnergyRevenue(energyRevenueInDay);

        newSpinReserveStatistics.setEfficacyRevenue(efficacyRevenueInDay);                        //效能費
        newSpinReserveStatistics.setAwardedCount(awardedCountInDay);                            //得標時數
        newSpinReserveStatistics.setTotalAwardedCapacity(totalAwardedCapacityInDay);            //總得標量
        newSpinReserveStatistics.setAvgCapacityPrice(avgCapacityPriceInDay);                    //平均得標價格


        //統計電能執行資料
        List<SpinReserveData> srDataInDay =
                spinReserveService.findSpinReserveDataBySrIdAndNoticeTypeAndStartTime(srId, NoticeType.UNLOAD, startTime, endTime);

        Integer noticeCountInDay = srDataInDay.size();

        //sr資料
        for (SpinReserveData srData : srDataInDay) {

            if (srData.getRevenuePrice() != null && srData.getRevenueFactor() != null) {

                avgRevenueFactorInDay = avgRevenueFactorInDay.add(srData.getRevenueFactor());
            }

        }

        if (avgRevenueFactorInDay != null && (avgRevenueFactorInDay.compareTo(BigDecimal.ZERO) > 0) && noticeCountInDay > 0) {

            avgRevenueFactorInDay = avgRevenueFactorInDay.divide(new BigDecimal(noticeCountInDay), 4, BigDecimal.ROUND_HALF_UP);

        } else {

            avgRevenueFactorInDay = BigDecimal.ZERO;

        }

        newSpinReserveStatistics.setAvgRevenueFactor(avgRevenueFactorInDay);
        newSpinReserveStatistics.setNoticeConut(noticeCountInDay);

        //field資料
        for (SpinReserveStatisticsDetail fieldStatistics : newSpinReserveStatistics.getList()) {

            Integer tempCount = 0;

            for (SpinReserveData srData : srDataInDay) {

                for (SpinReserveDataDetail fieldData : srData.getList()) {

                    if (fieldData.getFieldProfile().getId() == fieldStatistics.getFieldProfile().getId()) {

                        tempCount = tempCount + 1;
                        fieldStatistics.setAvgRevenueFactor(fieldStatistics.getAvgRevenueFactor().add(fieldData.getRevenueFactor()));
                    }

                }

            }

            fieldStatistics.setNoticeConut(tempCount);

            if (fieldStatistics.getAvgRevenueFactor() != null && (fieldStatistics.getAvgRevenueFactor().compareTo(BigDecimal.ZERO) > 0) && fieldStatistics.getNoticeConut() > 0) {

                fieldStatistics.setAvgRevenueFactor(fieldStatistics.getAvgRevenueFactor().divide(new BigDecimal(fieldStatistics.getNoticeConut()), 4, BigDecimal.ROUND_HALF_UP));

            } else {

                fieldStatistics.setAvgRevenueFactor(BigDecimal.ZERO);

            }

        }

        //Save 該日時間之srId統計資料
        spinReserveService.saveSpinReserveStatistics(newSpinReserveStatistics);
    }


    /***
     * 月績效統計 
     *
     * @param srId      SrID
     * @param startTime 起始時間(取該日00:00)
     *
     */
    public void monthlyRevenueStatisticsByIdAndTimeInMonth(Long srId, Date timestampStart) {

        Date startTime = timestampStart;
        Date endTime = timestampStart;
        Calendar tempCalender = Calendar.getInstance();

        BigDecimal capacityRevenueInMonth = new BigDecimal("0");
        BigDecimal energyRevenueInMonth = new BigDecimal("0");
        BigDecimal energyPriceInMonth = new BigDecimal("0");
        BigDecimal efficacyRevenueInMonth = new BigDecimal("0");
        BigDecimal totalAwardedCapacityInMonth = new BigDecimal("0");
        BigDecimal avgRevenueFactorInMonth = new BigDecimal("0");
        BigDecimal avgCapacityPriceInMonth = new BigDecimal("0");
        Integer noticeCountInMonth = 0;
        Integer noticeCountInMonthForCalculate = 0; //Sam 20201202
        Integer awardedCountInMonth = 0;
        Integer factorCountInMonth = 0;

        tempCalender.setTime(timestampStart);
        tempCalender.set(Calendar.MINUTE, 0);
        tempCalender.set(Calendar.SECOND, 0);
        tempCalender.set(Calendar.HOUR_OF_DAY, 0);
        tempCalender.set(Calendar.MILLISECOND, 0);
        tempCalender.set(Calendar.DAY_OF_MONTH, 1);
        startTime = tempCalender.getTime();

        //找當月最後一天  Sam20201023
        tempCalender.set(Calendar.DATE, tempCalender.getActualMaximum(Calendar.DATE));
        endTime = tempCalender.getTime();

        Optional<SpinReserveStatistics> spinReserveStatistics =
                spinReserveService.findSpinReserveStatistics(srId, StatisticsType.month, startTime);

        SpinReserveStatistics newSpinReserveStatistics;

        if (spinReserveStatistics.isPresent()) {
            newSpinReserveStatistics = spinReserveStatistics.get();
        } else {
            newSpinReserveStatistics = new SpinReserveStatistics();
        }

        newSpinReserveStatistics.setSpinReserveProfile(new SpinReserveProfile(srId));
        //強制更新時間為每月1號00:00(上午12:00)
        newSpinReserveStatistics.setTime(startTime);
        Date updateTime = new Date();
        newSpinReserveStatistics.setUpdateTime(updateTime);
        newSpinReserveStatistics.setStatisticsType(StatisticsType.month);


        List<SpinReserveStatistics> dailySpinReserveStatisticsList =
                spinReserveService.findSpinReserveStatisticsBySrIdAndStatisticsTypeAndTime(srId, StatisticsType.day, startTime, endTime);

        for (SpinReserveStatistics srStatisticsOfDay : dailySpinReserveStatisticsList) {

            if (srStatisticsOfDay.getCapacityRevenue() != null) {
                capacityRevenueInMonth = capacityRevenueInMonth.add(srStatisticsOfDay.getCapacityRevenue());
            }

            if (srStatisticsOfDay.getEnergyRevenue() != null) {
                energyRevenueInMonth = energyRevenueInMonth.add(srStatisticsOfDay.getEnergyRevenue());
            }

            if (srStatisticsOfDay.getEnergyPrice() != null) {
                energyPriceInMonth = energyPriceInMonth.add(srStatisticsOfDay.getEnergyPrice());
            }

            if (srStatisticsOfDay.getEfficacyRevenue() != null) {
                efficacyRevenueInMonth = efficacyRevenueInMonth.add(srStatisticsOfDay.getEfficacyRevenue());
            }

            if (srStatisticsOfDay.getAwardedCount() != null) {
                awardedCountInMonth = awardedCountInMonth + srStatisticsOfDay.getAwardedCount();
            }

            if (srStatisticsOfDay.getTotalAwardedCapacity() != null) {
                totalAwardedCapacityInMonth = totalAwardedCapacityInMonth.add(srStatisticsOfDay.getTotalAwardedCapacity());
            }


            if (srStatisticsOfDay.getAvgRevenueFactor() != null) {
                avgRevenueFactorInMonth = avgRevenueFactorInMonth.add(srStatisticsOfDay.getAvgRevenueFactor());
            }

            //Sam 20201202
            if ((srStatisticsOfDay.getNoticeConut() != null) && srStatisticsOfDay.getNoticeConut() > 0) {
                noticeCountInMonth = noticeCountInMonth + srStatisticsOfDay.getNoticeConut();
                //Sam 20201201 以天為單位計算通知數(因為每日執行率已為平均值)
                noticeCountInMonthForCalculate = noticeCountInMonthForCalculate + 1;
            }


        }

        //Sam 20201202 改用noticeCountInMonthForCalculate去計算月平均執行率
        if ((avgRevenueFactorInMonth.compareTo(BigDecimal.ZERO) > 0) && (noticeCountInMonthForCalculate > 0)) {
            //Sam 20201202
            avgRevenueFactorInMonth =
                    avgRevenueFactorInMonth.divide(new BigDecimal(noticeCountInMonthForCalculate), 4, BigDecimal.ROUND_HALF_UP);
            //avgRevenueFactorInMonth = avgRevenueFactorInMonth.divide(new BigDecimal(factorCountInMonth), 2, BigDecimal.ROUND_HALF_UP);
        }

        if ((capacityRevenueInMonth.compareTo(BigDecimal.ZERO) > 0) && (capacityRevenueInMonth != null)) {
            avgCapacityPriceInMonth = capacityRevenueInMonth.divide(totalAwardedCapacityInMonth, 0, BigDecimal.ROUND_HALF_UP);
        }

        newSpinReserveStatistics.setCapacityRevenue(capacityRevenueInMonth);                        //容量費

        //目前在spinReserveStatistics中， energyPrice 與 energyRevenue都是電能費，之後會統一用energyRevenue
        newSpinReserveStatistics.setEnergyPrice(energyPriceInMonth);                            //電能費
        newSpinReserveStatistics.setEnergyRevenue(energyRevenueInMonth);

        newSpinReserveStatistics.setEfficacyRevenue(efficacyRevenueInMonth);                        //效能費
        newSpinReserveStatistics.setAwardedCount(awardedCountInMonth);                            //得標時數
        newSpinReserveStatistics.setTotalAwardedCapacity(totalAwardedCapacityInMonth);            //總得標量
        newSpinReserveStatistics.setAvgCapacityPrice(avgCapacityPriceInMonth);                    //平均得標價格

        newSpinReserveStatistics.setAvgRevenueFactor(avgRevenueFactorInMonth);
        newSpinReserveStatistics.setNoticeConut(noticeCountInMonth);

        //field
        List<SpinReserveStatisticsDetail> fieldStatisticsDetailList = new ArrayList<>();
        //List<SpinReserveStatisticsDetail> fieldStatisticsDetailList = newSpinReserveStatistics.getList();
        Boolean fieldIdStatus = true;
        for (SpinReserveStatistics srStatisticsOfDay : dailySpinReserveStatisticsList) {

            for (SpinReserveStatisticsDetail fieldStatistics : srStatisticsOfDay.getList()) {

                if (fieldStatisticsDetailList != null) {

                    for (SpinReserveStatisticsDetail fieldStatisticsOfMonth : fieldStatisticsDetailList) {

                        if (fieldStatisticsOfMonth.getFieldProfile().getId() == fieldStatistics.getFieldProfile().getId()) {
                            fieldIdStatus = false;

                            fieldStatisticsOfMonth.setUpdateTime(new Date());

                            //容量費
                            fieldStatisticsOfMonth.setCapacityRevenue(
                                    fieldStatisticsOfMonth.getCapacityRevenue().add(fieldStatistics.getCapacityRevenue()));
                            //電能費(新)
                            fieldStatisticsOfMonth.setEnergyRevenue(
                                    fieldStatisticsOfMonth.getEnergyRevenue().add(fieldStatistics.getEnergyRevenue()));
                            //電能費(舊)
                            fieldStatisticsOfMonth.setEnergyPrice(
                                    fieldStatisticsOfMonth.getEnergyPrice().add(fieldStatistics.getEnergyRevenue()));
                            //效能費
                            fieldStatisticsOfMonth.setEfficacyRevenue(
                                    fieldStatisticsOfMonth.getEfficacyRevenue().add(fieldStatistics.getEfficacyRevenue()));
                            //得標時數
                            fieldStatisticsOfMonth.setAwardedCount(
                                    fieldStatisticsOfMonth.getAwardedCount() + fieldStatistics.getAwardedCount());
                            //總得標量
                            fieldStatisticsOfMonth.setTotalAwardedCapacity(
                                    fieldStatisticsOfMonth.getTotalAwardedCapacity().add(fieldStatistics.getTotalAwardedCapacity()));
                            //平均執行率
                            fieldStatisticsOfMonth.setAvgRevenueFactor(
                                    fieldStatisticsOfMonth.getAvgRevenueFactor().add(
                                            fieldStatistics.getAvgRevenueFactor().multiply(new BigDecimal(fieldStatistics.getNoticeConut()))));
                            //總調度次數
                            fieldStatisticsOfMonth.setNoticeConut(
                                    fieldStatisticsOfMonth.getNoticeConut() + fieldStatistics.getNoticeConut());

                        }

                    }

                }

                if (fieldIdStatus) {
                    //增加Detail資料
                    SpinReserveStatisticsDetail obj = new SpinReserveStatisticsDetail();
                    obj.setFieldProfile(new FieldProfile(fieldStatistics.getFieldProfile().getId()));
                    obj.setUpdateTime(new Date());
                    obj.setCreateTime(new Date());
                    obj.setCapacityRevenue(fieldStatistics.getCapacityRevenue());
                    obj.setEnergyPrice(fieldStatistics.getEnergyRevenue());
                    obj.setEnergyRevenue(fieldStatistics.getEnergyRevenue());
                    obj.setEfficacyRevenue(fieldStatistics.getEfficacyRevenue());
                    obj.setAwardedCount(fieldStatistics.getAwardedCount());
                    obj.setTotalAwardedCapacity(fieldStatistics.getTotalAwardedCapacity());
                    obj.setAvgCapacityPrice(new BigDecimal("0"));
                    obj.setAvgRevenueFactor(fieldStatistics.getAvgRevenueFactor().multiply(new BigDecimal(fieldStatistics.getNoticeConut())));
                    obj.setNoticeConut(fieldStatistics.getNoticeConut());

                    fieldStatisticsDetailList.add(obj);

                }


            }

        }

        for (SpinReserveStatisticsDetail fieldStatisticsOfMonth : fieldStatisticsDetailList) {

            if ((fieldStatisticsOfMonth.getCapacityRevenue().compareTo(BigDecimal.ZERO) > 0) &&
                    (fieldStatisticsOfMonth.getCapacityRevenue() != null) &&
                    (fieldStatisticsOfMonth.getTotalAwardedCapacity().compareTo(BigDecimal.ZERO) > 0)) {

                fieldStatisticsOfMonth.setAvgCapacityPrice(
                        fieldStatisticsOfMonth.getCapacityRevenue().divide(
                                fieldStatisticsOfMonth.getTotalAwardedCapacity(), 1, BigDecimal.ROUND_HALF_UP));

            }

            if ((fieldStatisticsOfMonth.getAvgRevenueFactor().compareTo(BigDecimal.ZERO) > 0) &&
                    (fieldStatisticsOfMonth.getAvgRevenueFactor() != null &&
                            (fieldStatisticsOfMonth.getNoticeConut() > 0))) {

                fieldStatisticsOfMonth.setAvgRevenueFactor(
                        fieldStatisticsOfMonth.getAvgRevenueFactor().divide(
                                new BigDecimal(fieldStatisticsOfMonth.getNoticeConut()), 0, BigDecimal.ROUND_HALF_UP));

            }

        }

        newSpinReserveStatistics.setList(fieldStatisticsDetailList);

        //Save 該日時間之srId統計資料
        spinReserveService.saveSpinReserveStatistics(newSpinReserveStatistics);
    }


}