package org.iii.esd.caculate;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.Constants;
import org.iii.esd.enums.DataType;
import org.iii.esd.enums.EnableStatus;
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
import org.iii.esd.mongo.service.QueryService;
import org.iii.esd.mongo.service.SpinReserveService;
import org.iii.esd.mongo.service.StatisticsService;
import org.iii.esd.mongo.service.UpdateService;
import org.iii.esd.sr.service.SpinReserveRevenueService;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {
        SpinReserveRevenueService.class,
        SpinReserveService.class,
        StatisticsService.class,
        QueryService.class,
        //AutomaticFrequencyControlService.class,
        UpdateService.class,
        FieldProfileService.class})
@EnableAutoConfiguration
@Log4j2
class SpinReserveRevenueTest extends AbstractServiceTest {

    private static final Long srId = 3L;
    //	@Value("${fieldId}")
    //	private Long fieldId;
    private Date timestampStart = new Date();    //1582819200000L
    private Date timestampEnd = new Date();    //1582819200000L
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    //private Date time1 = new Date();

    @Autowired
    private SpinReserveService spinReserveService;

    @Autowired
    private SpinReserveRevenueService spinReserveRevenueService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private FieldProfileService fieldProfileService;

    @Test
    public void testSpinReserveRevenueStatisticsByTime() {
        //計算時間
        try {
            timestampStart = Constants.TIMESTAMP_FORMAT.parse("2020-10-08 00:00:00");
            timestampEnd = Constants.TIMESTAMP_FORMAT.parse("2020-10-12 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Boolean flag = spinReserveRevenueService.spinReserveRevenueStatistics(timestampStart, timestampEnd);
        //log.info(flag);

        Boolean statusFlag = false;
        List<SpinReserveProfile> list = spinReserveService.findAllSpinReserveProfile();
        Long srId;
        for (SpinReserveProfile srList : list) {
            srId = srList.getId();
            statusFlag = spinReserveRevenueService.spinReserveRevenueStatisticsBySridAndTime(srId, timestampStart, timestampEnd);
        }

    }

    @Test
    public void testSpinReserveRevenueStatisticsBySridAndTime() {
        //計算時間
        try {
            timestampStart = Constants.TIMESTAMP_FORMAT.parse("2020-11-01 00:00:00");
            timestampEnd = Constants.TIMESTAMP_FORMAT.parse("2020-12-01 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Sam 20201022
        Long srId = 3L;
        Boolean flag = spinReserveRevenueService.spinReserveRevenueStatisticsBySridAndTime(srId, timestampStart, timestampEnd);
        log.info(flag);
    }

    @Test
    public void testSpinReserveRevenueStatistics() {

        //計算時間
        try {
            timestampStart = Constants.TIMESTAMP_FORMAT.parse("2020-11-01 00:00:00");
            timestampEnd = Constants.TIMESTAMP_FORMAT.parse("2020-12-01 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (timestampStart.before(timestampEnd)) {

            Date startTime = timestampStart;
            Date endTime = timestampEnd;

            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            Calendar next = Calendar.getInstance();

            start.setTime(timestampStart);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.HOUR_OF_DAY, 0);
            startTime = start.getTime();

            end.setTime(timestampEnd);
            end.set(Calendar.MINUTE, 0);
            end.set(Calendar.SECOND, 0);
            end.set(Calendar.HOUR_OF_DAY, 0);
            endTime = end.getTime();

            //統計該時段每日資料
            long diffInMillies = Math.abs(endTime.getTime() - startTime.getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            if (diff == 0) {
                diff = 1;
            }

            start.setTime(timestampStart);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.HOUR_OF_DAY, 0);
            startTime = start.getTime();

            end.setTime(timestampEnd);
            end.set(Calendar.MINUTE, 0);
            end.set(Calendar.SECOND, 0);
            end.set(Calendar.HOUR_OF_DAY, 0);
            endTime = end.getTime();

            for (int i = 0; i < diff; i++) {

                //計算容量費用
                Boolean flag = spinReserveRevenueService.capacityRevenueCalculate(startTime, endTime);

                start.add(Calendar.DATE, 1);
                startTime = start.getTime();
                end.add(Calendar.DATE, 1);
                endTime = end.getTime();
            }


            //取得srId List
            //Sam 20201022
            //List<SpinReserveProfile> list = spinReserveService.findAllSpinReserveProfile();
            //Sam 20201022
            Long srId = 3L;

            start.setTime(timestampStart);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.HOUR_OF_DAY, 0);
            startTime = start.getTime();

            end.setTime(timestampEnd);
            end.set(Calendar.MINUTE, 0);
            end.set(Calendar.SECOND, 0);
            end.set(Calendar.HOUR_OF_DAY, 0);
            endTime = end.getTime();

            //每日統計迴圈； 時間:startTime
            for (int i = 0; i < diff; i++) {

                next.setTime(startTime);// = start;
                next.add(Calendar.DATE, 1);
                Date nextTime = next.getTime();

                //srId迴圈
                //Sam 20201022
                //for(SpinReserveProfile srList : list) {

                //統計日績效資料
                //srId = srList.getId();

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

                BigDecimal capacityRevenueInDay = new BigDecimal("0");
                BigDecimal revenuePriceInDay = new BigDecimal("0");
                BigDecimal avgRevenueFactorInDay = new BigDecimal("0");
                //Sam 20201103
                BigDecimal avgCapacityPriceInDay = new BigDecimal("0");
                BigDecimal totalAwardedCapacityInDay = new BigDecimal("0");

                BigDecimal fieldCapacityRevenueInDay = new BigDecimal("0");
                BigDecimal fieldRevenuePriceInDay = new BigDecimal("0");
                BigDecimal fieldAvgRevenueFactorInDay = new BigDecimal("0");
                Integer fieldNoticeCountInDay;
                Integer fieldAwardedCountInDay;

                //統計電能執行資料
                List<SpinReserveData> srDataInDay =
                        spinReserveService.findSpinReserveDataBySrIdAndNoticeTypeAndStartTime(srId, NoticeType.UNLOAD, startTime, nextTime);
                Integer noticeCountInDay = srDataInDay.size();

                //sr資料
                for (SpinReserveData srData : srDataInDay) {

                    if (srData.getRevenuePrice() != null && srData.getRevenueFactor() != null) {
                        revenuePriceInDay = revenuePriceInDay.add(srData.getRevenuePrice());
                        avgRevenueFactorInDay = avgRevenueFactorInDay.add(srData.getRevenueFactor());
                    }

                }

                if (avgRevenueFactorInDay != null && (avgRevenueFactorInDay.compareTo(BigDecimal.ZERO) > 0) && noticeCountInDay > 0) {

                    avgRevenueFactorInDay = avgRevenueFactorInDay.divide(new BigDecimal(noticeCountInDay), 4, BigDecimal.ROUND_HALF_UP);

                } else {

                    avgRevenueFactorInDay = BigDecimal.ZERO;

                }

                //統計容量得標資料
                List<SpinReserveBid> srBidInDay = spinReserveService.findAllBySrIdAndTime(srId, startTime, nextTime);
                //Integer awardedCountInDay = srBidInDay.size();
                Integer awardedCountInDay = 0;

                //sr資料
                for (SpinReserveBid srBid : srBidInDay) {
                    //if(srBid.getCapacityRevenue() != null ) {
                    if ((srBid.getCapacityRevenue() != null) && (srBid.getCapacityRevenue().compareTo(BigDecimal.ZERO) > 0)) {
                        capacityRevenueInDay = capacityRevenueInDay.add(srBid.getCapacityRevenue());
                        awardedCountInDay = awardedCountInDay + 1;
                        //Sam 20201103
                        totalAwardedCapacityInDay = totalAwardedCapacityInDay.add(srBid.getAwarded_capacity());
                    }

                    //field資料
                }
                //Sam 20201103
                if ((capacityRevenueInDay.compareTo(BigDecimal.ZERO) > 0) && (capacityRevenueInDay != null)) {
                    avgCapacityPriceInDay = capacityRevenueInDay.divide(totalAwardedCapacityInDay, 0, BigDecimal.ROUND_HALF_UP);
                }
                newSpinReserveStatistics.setAvgCapacityPrice(avgCapacityPriceInDay);
                newSpinReserveStatistics.setTotalAwardedCapacity(totalAwardedCapacityInDay);

                newSpinReserveStatistics.setCapacityRevenue(capacityRevenueInDay);
                newSpinReserveStatistics.setEnergyPrice(revenuePriceInDay);
                newSpinReserveStatistics.setAvgRevenueFactor(avgRevenueFactorInDay);
                newSpinReserveStatistics.setNoticeConut(noticeCountInDay);
                newSpinReserveStatistics.setAwardedCount(awardedCountInDay);


                //field資料

                //建立fieldId List
                List<FieldProfile> fieldProfileList = fieldProfileService.findFieldProfileBySrId(srId, EnableStatus.enable);

                List<SpinReserveStatisticsDetail> fieldsrDataDetailList = new ArrayList<>();

                //建立detailList
                for (FieldProfile fieldProfile : fieldProfileList) {
                    Long fieldId = fieldProfile.getId();
                    SpinReserveStatisticsDetail obj = new SpinReserveStatisticsDetail();
                    obj.setFieldProfile(new FieldProfile(fieldId));
                    obj.setUpdateTime(new Date());
                    obj.setCreateTime(new Date());
                    obj.setCapacityRevenue(new BigDecimal("0"));
                    obj.setEnergyPrice(new BigDecimal("0"));
                    obj.setAvgRevenueFactor(new BigDecimal("0"));
                    obj.setNoticeConut(new Integer(0));
                    obj.setAwardedCount(new Integer(0));
                    fieldsrDataDetailList.add(obj);
                }

                //新增之前為Enable狀態之fieldId至detailList
                //檢查SpinReserveDataDetail資料
                for (SpinReserveData srData : srDataInDay) {

                    List<SpinReserveDataDetail> srDataDetailList = srData.getList();

                    if (srDataDetailList != null) {

                        for (SpinReserveDataDetail srDataDetail : srDataDetailList) {

                            Boolean fieldIdStatus = true;

                            for (SpinReserveStatisticsDetail fieldDetail : fieldsrDataDetailList) {

                                if (srDataDetail.getFieldProfile().getId() == fieldDetail.getFieldProfile().getId()) {
                                    fieldIdStatus = false;
                                }

                            }

                            if (fieldIdStatus) {
                                //增加Detail資料
                                SpinReserveStatisticsDetail obj = new SpinReserveStatisticsDetail();
                                obj.setFieldProfile(new FieldProfile(srDataDetail.getFieldProfile().getId()));
                                obj.setUpdateTime(new Date());
                                obj.setCreateTime(new Date());
                                obj.setCapacityRevenue(new BigDecimal("0"));
                                obj.setEnergyPrice(new BigDecimal("0"));
                                obj.setAvgRevenueFactor(new BigDecimal("0"));
                                obj.setNoticeConut(new Integer(0));
                                obj.setAwardedCount(new Integer(0));
                                fieldsrDataDetailList.add(obj);
                            }

                        }
                    }
                }
                //檢查SpinReserveBidDataDetail資料
                for (SpinReserveBid srBid : srBidInDay) {

                    List<SpinReserveBidDetail> srBidDetailList = srBid.getList();

                    if (srBidDetailList != null) {

                        for (SpinReserveBidDetail srBidDetail : srBidDetailList) {

                            Boolean fieldIdStatus = true;

                            for (SpinReserveStatisticsDetail fieldDetail : fieldsrDataDetailList) {

                                if (srBidDetail.getFieldProfile().getId() == fieldDetail.getFieldProfile().getId()) {
                                    fieldIdStatus = false;
                                }

                            }

                            if (fieldIdStatus) {
                                //增加Detail資料
                                SpinReserveStatisticsDetail obj = new SpinReserveStatisticsDetail();
                                obj.setFieldProfile(new FieldProfile(srBidDetail.getFieldProfile().getId()));
                                obj.setUpdateTime(new Date());
                                obj.setCreateTime(new Date());
                                obj.setCapacityRevenue(new BigDecimal("0"));
                                obj.setEnergyPrice(new BigDecimal("0"));
                                obj.setAvgRevenueFactor(new BigDecimal("0"));
                                obj.setNoticeConut(new Integer(0));
                                obj.setAwardedCount(new Integer(0));
                                fieldsrDataDetailList.add(obj);
                            }

                        }
                    }
                }

                //累加相同的fieldId資料
                for (SpinReserveStatisticsDetail fieldDetail : fieldsrDataDetailList) {

                    //***SpinReserveDataDetail資料***//
                    for (SpinReserveData srData : srDataInDay) {

                        List<SpinReserveDataDetail> srDataDetailList = srData.getList();

                        if (srDataDetailList != null) {

                            for (SpinReserveDataDetail srDataDetail : srDataDetailList) {

                                if (srDataDetail.getFieldProfile().getId() == fieldDetail.getFieldProfile().getId()) {

                                    Integer count = fieldDetail.getNoticeConut();
                                    BigDecimal price = new BigDecimal("0");
                                    BigDecimal nextPrice = new BigDecimal("0");

                                    if (fieldDetail.getEnergyPrice() != null) {
                                        price = fieldDetail.getEnergyPrice();
                                    }

                                    if (srDataDetail.getRevenuePrice() != null) {
                                        nextPrice = srDataDetail.getRevenuePrice();
                                    }

                                    fieldDetail.setNoticeConut(count + 1);
                                    //平均執行率與srId的相同
                                    fieldDetail.setAvgRevenueFactor(newSpinReserveStatistics.getAvgRevenueFactor());
                                    fieldDetail.setEnergyPrice(price.add(nextPrice));
                                }

                            }

                        }

                    }
                    //*****************************//

                    //***SpinReserveBidDetail資料 ***//
                    for (SpinReserveBid srBid : srBidInDay) {

                        List<SpinReserveBidDetail> srBidDetailList = srBid.getList();

                        if (srBidDetailList != null) {

                            for (SpinReserveBidDetail srBidDetail : srBidDetailList) {

                                if (srBidDetail.getFieldProfile().getId() == fieldDetail.getFieldProfile().getId()) {

                                    Integer awardedCount = fieldDetail.getAwardedCount();
                                    BigDecimal capacityPrice = new BigDecimal("0");
                                    BigDecimal nextCapacityPrice = new BigDecimal("0");

                                    if (fieldDetail.getCapacityRevenue() != null) {
                                        capacityPrice = fieldDetail.getCapacityRevenue();
                                    }

                                    if (srBidDetail.getCapacityRevenue() != null) {
                                        nextCapacityPrice = srBidDetail.getCapacityRevenue();
                                    }

                                    fieldDetail.setUpdateTime(new Date());
                                    fieldDetail.setCreateTime(new Date());
                                    //加入場域是否得標判斷 Sam 20201021
                                    if ((srBidDetail.getCapacityRevenue() != null) &&
                                            (srBidDetail.getCapacityRevenue().compareTo(BigDecimal.ZERO) > 0)) {
                                        fieldDetail.setAwardedCount(awardedCount + 1);
                                    }
                                    fieldDetail.setCapacityRevenue(capacityPrice.add(nextCapacityPrice));
                                }

                            }

                        }

                    }
                    //****************************//

                }

                newSpinReserveStatistics.setList(fieldsrDataDetailList);

                //Save 該日時間之srId統計資料
                spinReserveService.saveSpinReserveStatistics(newSpinReserveStatistics);
                //}

                //換下一日
                SimpleDateFormat sdf = Constants.TIMESTAMP_FORMAT;
                String dateStr = sdf.format(start.getTime());
                log.info(dateStr);
                start.add(Calendar.DATE, 1);
                startTime = start.getTime();
            }

            log.info("==========");

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

                next.setTime(startTime); // start;
                //next.add(Calendar.MONTH, 1);
                //找當月最後一天  Sam20201023
                next.set(Calendar.DATE, next.getActualMaximum(Calendar.DATE));
                Date nextTime = next.getTime();

                //Sam 20201022
                //for(SpinReserveProfile srList : list) {
                //Sam 20201022
                //srId = srList.getId();
                //統計月績效資料
                Optional<SpinReserveStatistics> spinReserveStatistics =
                        spinReserveService.findSpinReserveStatistics(srId, StatisticsType.month, startTime);

                SpinReserveStatistics newSpinReserveStatisticsInMonth;

                if (spinReserveStatistics.isPresent()) {
                    newSpinReserveStatisticsInMonth = spinReserveStatistics.get();
                } else {
                    newSpinReserveStatisticsInMonth = new SpinReserveStatistics();
                }

                newSpinReserveStatisticsInMonth.setSpinReserveProfile(new SpinReserveProfile(srId));
                //強制更新時間為每月 1號 00:00(上午12:00)
                newSpinReserveStatisticsInMonth.setTime(startTime);
                Date updateTime = new Date();
                newSpinReserveStatisticsInMonth.setUpdateTime(updateTime);
                newSpinReserveStatisticsInMonth.setStatisticsType(StatisticsType.month);

                BigDecimal capacityRevenueInMonth = new BigDecimal("0");
                BigDecimal energyPriceInMonth = new BigDecimal("0");
                BigDecimal avgRevenueFactorInMonth = new BigDecimal("0");
                Integer noticeCountInMonth = 0;
                Integer noticeCountInMonthForCalculate = 0; //Sam 20201202
                Integer awardedCountInMonth = 0;
                Integer factorCountInMonth = 0;

                List<SpinReserveStatistics> spinReserverStatisticsOfDayList =
                        spinReserveService.findSpinReserveStatisticsBySrIdAndStatisticsTypeAndTime(
                                srId, StatisticsType.day, startTime, nextTime);


                //統計srId in Month資料
                for (SpinReserveStatistics srStatisticsOfDay : spinReserverStatisticsOfDayList) {

                    if (srStatisticsOfDay.getCapacityRevenue() != null) {
                        capacityRevenueInMonth = capacityRevenueInMonth.add(srStatisticsOfDay.getCapacityRevenue());
                    }

                    if (srStatisticsOfDay.getEnergyPrice() != null) {
                        energyPriceInMonth = energyPriceInMonth.add(srStatisticsOfDay.getEnergyPrice());
                    }

                    if (srStatisticsOfDay.getAvgRevenueFactor() != null) {
                        avgRevenueFactorInMonth = avgRevenueFactorInMonth.add(srStatisticsOfDay.getAvgRevenueFactor());
                    }

                    //Sam 20201202
                    if ((srStatisticsOfDay.getNoticeConut() != null) && srStatisticsOfDay.getNoticeConut() > 0) {
                        noticeCountInMonth = noticeCountInMonth + srStatisticsOfDay.getNoticeConut();
                        //Sam 20201201 以天為單位計算通知數(因為美日執行率已為平均值)
                        noticeCountInMonthForCalculate = noticeCountInMonthForCalculate + 1;
                    }

                    if (srStatisticsOfDay.getAwardedCount() != null) {
                        awardedCountInMonth = awardedCountInMonth + srStatisticsOfDay.getAwardedCount();
                    }

                    factorCountInMonth = factorCountInMonth + 1;

                }

                //Sam 20201202 改用noticeCountInMonthForCalculate去計算月平均執行率
                if ((avgRevenueFactorInMonth.compareTo(BigDecimal.ZERO) > 0) && (noticeCountInMonthForCalculate > 0)) {
                    //Sam 20201202
                    avgRevenueFactorInMonth =
                            avgRevenueFactorInMonth.divide(new BigDecimal(noticeCountInMonthForCalculate), 4, BigDecimal.ROUND_HALF_UP);
                    //avgRevenueFactorInMonth = avgRevenueFactorInMonth.divide(new BigDecimal(factorCountInMonth), 2, BigDecimal.ROUND_HALF_UP);
                }

                newSpinReserveStatisticsInMonth.setCapacityRevenue(capacityRevenueInMonth);
                newSpinReserveStatisticsInMonth.setEnergyPrice(energyPriceInMonth);
                newSpinReserveStatisticsInMonth.setAvgRevenueFactor(avgRevenueFactorInMonth);
                newSpinReserveStatisticsInMonth.setNoticeConut(noticeCountInMonth);
                newSpinReserveStatisticsInMonth.setAwardedCount(awardedCountInMonth);

                //*******************************************************************//

                //建立fieldId List
                List<FieldProfile> fieldProfileList = fieldProfileService.findFieldProfileBySrId(srId, EnableStatus.enable);

                List<SpinReserveStatisticsDetail> fieldsrDataDetailListInMonth = new ArrayList<>();
                //建立detailList
                for (FieldProfile fieldProfile : fieldProfileList) {
                    Long fieldId = fieldProfile.getId();
                    SpinReserveStatisticsDetail obj = new SpinReserveStatisticsDetail();
                    obj.setFieldProfile(new FieldProfile(fieldId));
                    obj.setUpdateTime(new Date());
                    obj.setCreateTime(new Date());
                    obj.setCapacityRevenue(new BigDecimal("0"));
                    obj.setEnergyPrice(new BigDecimal("0"));
                    obj.setAvgRevenueFactor(new BigDecimal("0"));
                    obj.setNoticeConut(new Integer(0));
                    obj.setAwardedCount(new Integer(0));
                    fieldsrDataDetailListInMonth.add(obj);
                }

                //新增之前為Enable狀態之fieldId至detailList
                //檢查SpinReserveDataDetail資料
                for (SpinReserveStatistics srStatisticsOfDay : spinReserverStatisticsOfDayList) {

                    List<SpinReserveStatisticsDetail> srStatisticsDetailList = srStatisticsOfDay.getList();

                    if (srStatisticsDetailList != null) {

                        for (SpinReserveStatisticsDetail srStatisticsDetail : srStatisticsDetailList) {

                            Boolean fieldIdStatus = true;

                            for (SpinReserveStatisticsDetail fieldDetail : fieldsrDataDetailListInMonth) {

                                if (srStatisticsDetail.getFieldProfile().getId() == fieldDetail.getFieldProfile().getId()) {
                                    fieldIdStatus = false;
                                }

                            }

                            if (fieldIdStatus) {
                                //增加Detail資料
                                SpinReserveStatisticsDetail obj = new SpinReserveStatisticsDetail();
                                obj.setFieldProfile(new FieldProfile(srStatisticsDetail.getFieldProfile().getId()));
                                obj.setUpdateTime(new Date());
                                obj.setCreateTime(new Date());
                                obj.setCapacityRevenue(new BigDecimal("0"));
                                obj.setEnergyPrice(new BigDecimal("0"));
                                obj.setAvgRevenueFactor(new BigDecimal("0"));
                                obj.setNoticeConut(new Integer(0));
                                obj.setAwardedCount(new Integer(0));
                                fieldsrDataDetailListInMonth.add(obj);
                            }

                        }
                    }
                }


                for (SpinReserveStatisticsDetail fieldStatisticsDetailInMonth : fieldsrDataDetailListInMonth) {

                    //統計fieldId in Month資料
                    BigDecimal fieldCapacityRevenueInMonth = new BigDecimal("0");
                    BigDecimal fieldEnergyPriceInMonth = new BigDecimal("0");
                    BigDecimal fieldAvgRevenueFactorInMonth = new BigDecimal("0");
                    Integer fieldNoticeCountInMonth = 0;
                    Integer fieldNoticeCountInMonthForCalculate = 0; //Sam 20201202
                    Integer fieldAwardedCountInMonth = 0;
                    Integer fieldFactorCountInMonth = 0;

                    for (SpinReserveStatistics srStatisticsOfDay : spinReserverStatisticsOfDayList) {

                        for (SpinReserveStatisticsDetail fieldStatisticsDetail : srStatisticsOfDay.getList()) {

                            if (fieldStatisticsDetail.getFieldProfile().getId() == fieldStatisticsDetailInMonth.getFieldProfile().getId()) {

                                if (fieldStatisticsDetail.getCapacityRevenue() != null) {
                                    fieldCapacityRevenueInMonth =
                                            fieldCapacityRevenueInMonth.add(fieldStatisticsDetail.getCapacityRevenue());
                                }

                                if (fieldStatisticsDetail.getEnergyPrice() != null) {
                                    fieldEnergyPriceInMonth = fieldEnergyPriceInMonth.add(fieldStatisticsDetail.getEnergyPrice());
                                }

                                if (fieldStatisticsDetail.getAvgRevenueFactor() != null) {
                                    fieldAvgRevenueFactorInMonth =
                                            fieldAvgRevenueFactorInMonth.add(fieldStatisticsDetail.getAvgRevenueFactor());
                                }

                                //Sam 20201202
                                if (fieldStatisticsDetail.getNoticeConut() != null && fieldStatisticsDetail.getNoticeConut() > 0) {
                                    fieldNoticeCountInMonth = fieldNoticeCountInMonth + fieldStatisticsDetail.getNoticeConut();
                                    fieldNoticeCountInMonthForCalculate = fieldNoticeCountInMonthForCalculate + 1; //Sam 20201202
                                }

                                if (fieldStatisticsDetail.getAwardedCount() != null) {
                                    fieldAwardedCountInMonth = fieldAwardedCountInMonth + fieldStatisticsDetail.getAwardedCount();
                                }

                                fieldFactorCountInMonth = fieldFactorCountInMonth + 1;

                                fieldStatisticsDetailInMonth.setCapacityRevenue(fieldCapacityRevenueInMonth);
                                fieldStatisticsDetailInMonth.setEnergyPrice(fieldEnergyPriceInMonth);
                                fieldStatisticsDetailInMonth.setAvgRevenueFactor(fieldAvgRevenueFactorInMonth);
                                fieldStatisticsDetailInMonth.setNoticeConut(fieldNoticeCountInMonth);
                                fieldStatisticsDetailInMonth.setAwardedCount(fieldAwardedCountInMonth);

                            }

                        }

                    }
                    //Sam 20201202
                    if ((fieldAvgRevenueFactorInMonth.compareTo(BigDecimal.ZERO) > 0) && (fieldNoticeCountInMonthForCalculate > 0)) {
                        //Sam 20201202
                        fieldAvgRevenueFactorInMonth = fieldAvgRevenueFactorInMonth
                                .divide(new BigDecimal(fieldNoticeCountInMonthForCalculate), 4, BigDecimal.ROUND_HALF_UP);
                        //fieldAvgRevenueFactorInMonth = fieldAvgRevenueFactorInMonth.divide(new BigDecimal(fieldFactorCountInMonth), 2, BigDecimal.ROUND_HALF_UP);
                    }

                    fieldStatisticsDetailInMonth.setAvgRevenueFactor(fieldAvgRevenueFactorInMonth);
                }

                newSpinReserveStatisticsInMonth.setList(fieldsrDataDetailListInMonth);

                //Save 該日時間之srId統計資料
                spinReserveService.saveSpinReserveStatistics(newSpinReserveStatisticsInMonth);

                //}

                SimpleDateFormat sdf = Constants.TIMESTAMP_FORMAT;
                String dateStr = sdf.format(start.getTime());
                log.info(dateStr);
                start.add(Calendar.MONTH, 1);
            }

            log.info(startYear + "年" + startMonth + "月" + " and " + endYear + "年" + endMonth + "月");

        } else {
            log.info("時間錯誤");
        }

    }

    @Test
    public void spinReserveRevenueStatisticsByMonth() {

        //計算時間
        try {
            timestampStart = Constants.TIMESTAMP_FORMAT.parse("2020-02-28 09:00:00");
            timestampEnd = Constants.TIMESTAMP_FORMAT.parse("2020-02-28 11:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Date startTime = timestampStart;
        Date endTime = timestampEnd;

        Calendar start = Calendar.getInstance();

        start.setTime(timestampStart);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.HOUR_OF_DAY, 0);
        startTime = start.getTime();

        Boolean flag = spinReserveRevenueService.capacityRevenueCalculate(startTime, endTime);

        //取月時間


    }


    @Test
    public void capacityRevenueCalculateByTime() {

        //計算時間
        try {
            timestampStart = Constants.TIMESTAMP_FORMAT.parse("2021-02-16 00:00:00");
            timestampEnd = Constants.TIMESTAMP_FORMAT.parse("2021-02-17 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Date startTime = timestampStart;
        Date endTime = timestampEnd;

        Calendar start = Calendar.getInstance();

        start.setTime(timestampStart);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.HOUR_OF_DAY, 0);
        startTime = start.getTime();

        Boolean flag = spinReserveRevenueService.capacityRevenueCalculate(startTime, endTime);

    }

    @Test
    public void energyRevenueCalculateBysrIdAndTime() {

        //計算時間
        try {
            timestampStart = Constants.TIMESTAMP_FORMAT.parse("2020-02-19 09:00:00");
            timestampEnd = Constants.TIMESTAMP_FORMAT.parse("2020-02-19 11:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Long srId = 1L;

        Date startTime = timestampStart;
        Date endTime = timestampEnd;

        Calendar start = Calendar.getInstance();

        start.setTime(timestampStart);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.HOUR_OF_DAY, 0);
        startTime = start.getTime();

        Boolean flag = spinReserveRevenueService.energyRevenueCalculate(srId, startTime, endTime);

    }

    @Test
    public void capacityRevenueCalculate() {

        //計算時間
        try {
            timestampStart = Constants.TIMESTAMP_FORMAT.parse("2020-09-11 00:00:00");
            timestampEnd = Constants.TIMESTAMP_FORMAT.parse("2020-09-12 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Date startTime = timestampStart;
        Date endTime = timestampEnd;

        Calendar start = Calendar.getInstance();

        start.setTime(timestampStart);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.HOUR_OF_DAY, 0);
        startTime = start.getTime();

        List<SpinReserveProfile> list = spinReserveService.findAllSpinReserveProfile();
        Long srId;

        for (SpinReserveProfile srList : list) {
            srId = srList.getId();
            List<SpinReserveBid> spinReserveBidList =
                    spinReserveRevenueService.getCapacityRevenue(spinReserveService.findAllBySrIdAndTime(srId, startTime));

            if (spinReserveBidList != null && spinReserveBidList.size() > 0) {

                //Save spinReserveBidList
                //spinReserveService.addOrUpdateAll(srId, spinReserveBidList);

                for (SpinReserveBid spinReserveBid : spinReserveBidList) {
                    log.info(spinReserveBid);
                }
            }

        }

    }

    @Test
    public void energyRevenueCalculate() {
        /**
         * TODO EnergyRevenue(電能費)測試
         */
        //計算時間
        try {
            timestampStart = Constants.TIMESTAMP_FORMAT.parse("2020-09-19 15:00:00");
            timestampEnd = Constants.TIMESTAMP_FORMAT.parse("2020-09-19 20:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Date startTime = timestampStart;
        Date endTime = timestampEnd;

        List<SpinReserveProfile> list = spinReserveService.findAllSpinReserveProfile();
        Long srId;


        //for(SpinReserveProfile srList : list) {
        srId = list.get(2).getId();
        //時段內srId的EnergyRevenue計算
        List<SpinReserveData> spinReserveDataList = spinReserveRevenueService.getEnergyRevenue(srId,
                spinReserveService.findSpinReserveDataBySrIdAndNoticeTypeAndNoticeTime(srId, NoticeType.UNLOAD, startTime, endTime));

        if (spinReserveDataList != null && spinReserveDataList.size() > 0) {

            //Save spinReserveDataList
            spinReserveService.saveSpinReserveData(spinReserveDataList);

            for (SpinReserveData spinReserveData : spinReserveDataList) {
                log.info(spinReserveData);
            }
        }


        //}

    }


    @Test
    public void CapacityRevenueCalculatBySridAndStartTime() {

        //計算時間
        try {
            timestampStart = Constants.TIMESTAMP_FORMAT.parse("2020-09-23 00:00:00");
            timestampEnd = Constants.TIMESTAMP_FORMAT.parse("2020-09-24 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Date startTime = timestampStart;
        Date endTime = timestampEnd;

        Calendar start = Calendar.getInstance();

        start.setTime(timestampStart);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.HOUR_OF_DAY, 0);
        startTime = start.getTime();

        //srId整天capacityRevenue計算
        List<SpinReserveBid> spinReserveBidList =
                spinReserveRevenueService.getCapacityRevenue(spinReserveService.findAllBySrIdAndTime(srId, startTime));

        if (spinReserveBidList != null && spinReserveBidList.size() > 0) {
            for (SpinReserveBid spinReserveBid : spinReserveBidList) {
                log.info(spinReserveBid);
            }
        } else {
            fail();
        }

    }

    @Test
    public void EnergyRevenueCalculatBySridAndStartTime() {

        //計算時間
        try {
            timestampStart = Constants.TIMESTAMP_FORMAT.parse("2020-09-19 15:00:00");
            timestampEnd = Constants.TIMESTAMP_FORMAT.parse("2020-09-19 20:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Long srId = 3L;

        //時段內srId的EnergyRevenue計算
        List<SpinReserveData> spinReserveDataList = spinReserveRevenueService.getEnergyRevenue(srId, spinReserveService
                .findSpinReserveDataBySrIdAndNoticeTypeAndNoticeTime(srId, NoticeType.UNLOAD, timestampStart, timestampEnd));

        if (spinReserveDataList != null && spinReserveDataList.size() > 0) {
            for (SpinReserveData spinReserveData : spinReserveDataList) {
                log.info(spinReserveData);
            }
        } else {
            fail();
        }

    }

    @Test
    public void CapacityRevenueCalculatTest() {


        try {
            timestampStart = Constants.TIMESTAMP_FORMAT.parse("2021-02-16 00:00:00");
            timestampEnd = Constants.TIMESTAMP_FORMAT.parse("2021-02-17 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Calendar start = Calendar.getInstance();

        start.setTime(timestampStart);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.HOUR_OF_DAY, 0);
        timestampStart = start.getTime();

        List<SpinReserveBid> spinReserveBidList = spinReserveService.findAllBySrIdAndTime(srId, timestampStart);

        List<SpinReserveBid> spinReserveBidListCalculated = spinReserveService.findAllBySrIdAndTime(srId, timestampStart);

        for (SpinReserveBid data : spinReserveBidListCalculated) {
            //if( OperatorStatus.READY.equals(data.getOperatorStatus())) {
            //if( !OperatorStatus.QUIT.equals(data.getOperatorStatus())) { //Sam 20210218
            BigDecimal awardedCapacity = data.getAwarded_capacity();
            BigDecimal capacityPrice = data.getSr_price();
            BigDecimal capacityRevenue = new BigDecimal("0");

            if (awardedCapacity != null && capacityPrice != null) {
                capacityRevenue = awardedCapacity.multiply(capacityPrice);
            }
            //更新 ListCalculated物件
            data.setCapacityRevenue(capacityRevenue);

            List<SpinReserveBidDetail> list = data.getList();
            if (list != null) {
                for (SpinReserveBidDetail objectList : list) {
                    BigDecimal listAwardedCapacity = objectList.getAwarded_capacity();
                    BigDecimal listCapacityPrice = capacityPrice;
                    BigDecimal listCapacityRevenue = new BigDecimal("0");
                    if (listAwardedCapacity != null && capacityPrice != null) {
                        listCapacityRevenue = listAwardedCapacity.multiply(listCapacityPrice);
                    }
                    //更新 List物件
                    objectList.setCapacityRevenue(listCapacityRevenue);
                }
            }

            //} //Sam 20210218
        }


        if (spinReserveBidList != null && spinReserveBidList.size() > 0 &&
                (spinReserveBidList.size() == spinReserveBidListCalculated.size())) {
            for (SpinReserveBid spinReserveBid : spinReserveBidList) {
                log.info(spinReserveBid);
            }
            for (SpinReserveBid spinReserveBidCalculate : spinReserveBidListCalculated) {
                log.info(spinReserveBidCalculate);
            }
        } else {
            fail();
        }

    }

    @Test
    public void EnergyRevenueCalculatTest() {

        //查詢時間
        String time = "2020-03-19 10:37:22";

        try {
            timestampStart = sdf.parse(time);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            timestampStart = Constants.TIMESTAMP_FORMAT.parse("2020-11-26 00:00:00");
            timestampEnd = Constants.TIMESTAMP_FORMAT.parse("2020-11-26 23:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<SpinReserveData> spinReserveData = spinReserveService
                .findSpinReserveDataBySrIdAndNoticeTypeAndNoticeTime(srId, NoticeType.UNLOAD, timestampStart, timestampEnd);

        BigDecimal srEnergyPrice = new BigDecimal("0");

        BigDecimal srEnergyRevenue = new BigDecimal("0");
        //Sam 20201022
        //BigDecimal srEnergyRevenue_MWh = new BigDecimal("0");
        //BigDecimal MWhFactor = new BigDecimal("1000");

        BigDecimal srBaseLine = new BigDecimal("0");

        BigDecimal srClippedKW = new BigDecimal("0");
        BigDecimal srLoadKW = new BigDecimal("0");

        BigDecimal srClipKW = new BigDecimal("0");
        int clipKW = 0;

        for (SpinReserveData srData : spinReserveData) {

            srData.setSpinReserveProfile(new SpinReserveProfile(srId));

            //取得SpinReserveData/energyPrice
            srEnergyPrice = srData.getEnergyPrice();

            //取得BaseLine
            srBaseLine = srData.getBaseline();

            //取得通知降載量
            clipKW = srData.getClipKW();
            srClipKW = BigDecimal.valueOf((int) clipKW);

            BigDecimal revenueFactor = new BigDecimal("0");

            srClippedKW = new BigDecimal("0");
            srLoadKW = new BigDecimal("0"); //Sam 20201128

            //計算執行每分鐘平均降載容量，包含SR、Field A、B、C.....
            List<SpinReserveDataDetail> detailList = srData.getList();

            timestampStart = srData.getStartTime();
            timestampEnd = srData.getEndTime();

            //Sam 20201029 取 整分 資料，台電計算資料區間為61筆；e.g. 18:15:00 ~ 19:15:00
            Calendar endTime = Calendar.getInstance();
            endTime.setTime(timestampEnd);
            if (endTime.get(Calendar.SECOND) > 0) {
                endTime.add(Calendar.MINUTE, 1);
                endTime.set(Calendar.SECOND, 0);
            }
            timestampEnd = endTime.getTime();

            for (SpinReserveDataDetail fieldList : detailList) {
                //Long fieldID = fieldList.getFieldProfile().getId();
                Long fieldID = 6L; //for test Sam 20201128
                List<ElectricData> electricDataList =
                        statisticsService.findElectricDataByFieldIdAndDataTypeAndTime(fieldID, DataType.T99, timestampStart, timestampEnd);

                //計算參數歸零
                BigDecimal fieldAccActivePower = new BigDecimal("0");
                BigDecimal activePower = new BigDecimal("0");
                BigDecimal fieldBaseLine = new BigDecimal("0");
                BigDecimal fieldClippedKW = new BigDecimal("0");
                BigDecimal activePowerNum = new BigDecimal(electricDataList.size());

                for (ElectricData electricData : electricDataList) {
                    activePower = electricData.getActivePower();
                    //field執行期間功率累加總和
                    fieldAccActivePower = fieldAccActivePower.add(activePower);
                }

                //field baseline
                fieldBaseLine = fieldList.getBaseline();

                //field執行期間平均用電量
                if (fieldAccActivePower.compareTo(BigDecimal.ZERO) > 0) {
                    //計算至小數一位，第二位四捨五入 //Sam 20201029
                    fieldClippedKW = fieldAccActivePower.divide(activePowerNum, 1, BigDecimal.ROUND_HALF_UP);

                }

                srLoadKW = srLoadKW.add(fieldClippedKW);

                //field執行期間平均達成容量
                fieldClippedKW = fieldBaseLine.subtract(fieldClippedKW);
                fieldList.setClippedKW(fieldClippedKW);

                //統計SRID執行期間平均達成容量
                srClippedKW = srClippedKW.add(fieldClippedKW);
            }

            //計算執行達成容量 srId clippedKW
            srClippedKW = srBaseLine.subtract(srLoadKW);
            //紀錄SRID執行期間平均達成容量;要確認與(srBaseLine - field加總平均用電量)是否相符
            srData.setClippedKW(srClippedKW);

            //計算輔助服務執行率 srId revenueFactor
            if (srClippedKW.compareTo(BigDecimal.ZERO) > 0) {
                //Sam 20201023 //20201029 修正小數位數 ==> 1.000 (100.0%)
                revenueFactor = srClippedKW.divide(srClipKW, 3, BigDecimal.ROUND_HALF_UP);
            }

            revenueFactor = revenueFactor.multiply(new BigDecimal("100"));

            //紀錄執行率
            srData.setRevenueFactor(revenueFactor);

            //電能費計算
            BigDecimal revenueFactor115 = new BigDecimal("115");
            BigDecimal revenueFactor95 = new BigDecimal("95");
            BigDecimal revenueFactor80 = new BigDecimal("80");

            BigDecimal energyPriceFactorOver115 = new BigDecimal("0.50");
            BigDecimal energyPriceFactor = new BigDecimal("0.00");
            BigDecimal srClippedKWOver115 = new BigDecimal("0.00");

            BigDecimal srClippedKW115 = new BigDecimal("0.00");
            srClippedKW115 = srClipKW.multiply(new BigDecimal("1.15"));

            if (revenueFactor.compareTo(revenueFactor115) >= 1) {
                // >115%
                srClippedKWOver115 = srClippedKW.subtract(srClippedKW115);
                energyPriceFactor = new BigDecimal("1.00");

            } else if (revenueFactor.compareTo(revenueFactor115) < 1 && revenueFactor.compareTo(revenueFactor95) >= 1) {
                // >95% && <=115%
                energyPriceFactor = new BigDecimal("1.00");

            } else if (revenueFactor.compareTo(revenueFactor95) < 1 && revenueFactor.compareTo(revenueFactor80) >= 1) {
                // >80% && <= 95%
                energyPriceFactor = new BigDecimal("0.90");

            } else {
                // <= 80%
                energyPriceFactor = new BigDecimal("0.80");
            }

            //取得執行時間
            Date start = srData.getStartTime();
            Date end = srData.getEndTime();
            long diff = end.getTime() - start.getTime();
            float diffhours = (float) ((float) diff / (60 * 60 * 1000));

            BigDecimal tempRevenue = new BigDecimal("0.00");
            BigDecimal tempRevenue115 = new BigDecimal("0.00");
            BigDecimal durationHours = BigDecimal.valueOf(diffhours);

            //執行達成容量*電能因子(115%以下)
            tempRevenue = energyPriceFactor.multiply(srClippedKW.subtract(srClippedKWOver115));
            //執行達成容量*電能因子(115%以上)
            tempRevenue115 = energyPriceFactorOver115.multiply(srClippedKWOver115);

            //執行達成容量*電能因子(115%以下 + 115%以上 )
            srEnergyRevenue = tempRevenue.add(tempRevenue115);

            //y*電能報價
            srEnergyRevenue = srEnergyRevenue.multiply(srEnergyPrice);
            srEnergyRevenue = srEnergyRevenue.divide(new BigDecimal("1000.000"), 3, BigDecimal.ROUND_HALF_UP);
            //y*執行時間
            srEnergyRevenue = srEnergyRevenue.multiply(durationHours).setScale(0, BigDecimal.ROUND_HALF_UP);
            //y/1000 Sam20201022

            //紀錄電能費用
            srData.setRevenuePrice(srEnergyRevenue);

            //分配field energyRevenue
            for (SpinReserveDataDetail fieldList : detailList) {
                BigDecimal clippedKWPercentage = new BigDecimal("0.00");
                if (fieldList.getClippedKW().compareTo(BigDecimal.ZERO) > 0) {

                    clippedKWPercentage = fieldList.getClippedKW().divide(srClippedKW, 3, BigDecimal.ROUND_HALF_UP);
                }

                fieldList.setRevenuePrice(clippedKWPercentage.multiply(srEnergyRevenue).setScale(1, BigDecimal.ROUND_HALF_UP));
            }

        }

    }

}
