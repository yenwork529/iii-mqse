package org.iii.esd.caculate;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import lombok.extern.log4j.Log4j2;
import org.iii.esd.mongo.repository.SpinReserveBidRepository;
import org.iii.esd.utils.DatetimeUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.Constants;
import org.iii.esd.enums.NoticeType;
import org.iii.esd.enums.AncillaryServiceType;
import org.iii.esd.mongo.document.SpinReserveBid;
import org.iii.esd.mongo.document.SpinReserveData;
import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.mongo.service.QueryService;
import org.iii.esd.mongo.service.SpinReserveService;
import org.iii.esd.mongo.service.StatisticsService;
import org.iii.esd.mongo.service.UpdateService;
import org.iii.esd.sr.service.SpinReserveRevenueService;
import org.iii.esd.sr.service.SrSupRevenueService;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {
        SpinReserveRevenueService.class,
		SrSupRevenueService.class,
        SpinReserveService.class,
        StatisticsService.class,
        QueryService.class,
        //AutomaticFrequencyControlService.class,
        UpdateService.class,
        FieldProfileService.class})
@EnableAutoConfiguration
@Log4j2
class SrSupRevenueTest extends AbstractServiceTest {

    private static final Long srId = 5L;
    //	@Value("${fieldId}")
    //	private Long fieldId;
    private Date timestampStart = new Date();    //1582819200000L
    private Date timestampEnd = new Date();    //1582819200000L
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    //private Date time1 = new Date();

    @Autowired
    private SrSupRevenueService srSupRevenueService;
    
    @Autowired
    private SpinReserveService spinReserveService;

    @Autowired
    private SpinReserveRevenueService spinReserveRevenueService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private FieldProfileService fieldProfileService;
//
//    @Autowired
//    private SpinReserveBid spinReserveBid;
//
    @Autowired
    private SpinReserveBidRepository spinReserveBidRepo;
    
    



    
    @Test
    public void testSpinReserveRevenueStatisticsByTime() {
        //計算時間
        try {
            timestampStart = Constants.TIMESTAMP_FORMAT.parse("2021-07-07 00:00:00");
            timestampEnd = Constants.TIMESTAMP_FORMAT.parse("2021-07-08 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }


        Boolean statusFlag = false;
    
        Long srId = (long) 5;
       
        srSupRevenueService.spinReserveRevenueStatisticsBySrIdAndTime(srId, timestampStart, timestampEnd, AncillaryServiceType.sr);

    }

    @Test
    public void testGetCapacityRevenue() throws ParseException {


        Date end = yyyyMMddHHmmss.parse("20210706 01:10:00"); //new Date();
        Date start = DatetimeUtils.add(end, Calendar.MINUTE, -30);
//        srId = (long) 5;
        List<SpinReserveBid> list = spinReserveBidRepo.findBySrIdAndTime(srId, start, end);
        log.info(list.size());
        log.info(list.get(0).getUpdateTime());
//        srSupRevenueService.getCapacityRevenue(list);

//        SpinReserveBid spinReserveBid = new SpinReserveBid();
//        spinReserveBid.setAwarded_capacity(new BigDecimal("2"));
//        spinReserveBid.setSr_capacity(new BigDecimal("3"));
//        spinReserveBid.setServiceFactor(new BigDecimal("4"));
        list.get(0).setAwarded_capacity(new BigDecimal("2"));
        list.get(0).setSr_price(new BigDecimal("3"));
        list.get(0).setServiceFactor(new BigDecimal("4"));
        srSupRevenueService.getCapacityRevenue(list);
        log.info("capcity:"+list.get(0).getAwarded_capacity());
        log.info("Sr price:"+list.get(0).getSr_price());
        log.info("Factor:"+list.get(0).getServiceFactor());
        log.info("Revenue:"+list.get(0).getCapacityRevenue());

        //缺上傳DB function


//
//        List<SpinReserveBid> list = new LinkedList<SpinReserveBid>();
//        list.add(spinReserveBid);
//        srSupRevenueService.getCapacityRevenue(list).toString();
           
        }

    @Test
    public void testRevenueCalculateByBySridAndTime() {
    	//計算時間
        try {
            timestampStart = Constants.TIMESTAMP_FORMAT.parse("2021-07-07 00:00:00");
            timestampEnd = Constants.TIMESTAMP_FORMAT.parse("2021-07-08 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Long srId = (long)5;
        srSupRevenueService.revenueCalculateBySridAndTime(srId, timestampStart, timestampEnd);
    	
    }
    
	@Test
	public void testServiceParametersCalculateBySridAndTimeAndServiceType() {
		//計算時間
        try {
            timestampStart = Constants.TIMESTAMP_FORMAT.parse("2021-07-07 00:00:00");
            timestampEnd = Constants.TIMESTAMP_FORMAT.parse("2021-07-08 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Long srId = (long)5;
        AncillaryServiceType serviceType = AncillaryServiceType.sr; 
        
        srSupRevenueService.serviceParametersCalculate(srId, timestampStart, timestampEnd, serviceType);
		
	}
	    
    @Test
    public void testSrSupPerformanceCalBySrid() {
        //計算時間
        try {
            timestampStart = Constants.TIMESTAMP_FORMAT.parse("2021-07-07 00:00:00");
            timestampEnd = Constants.TIMESTAMP_FORMAT.parse("2021-07-08 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Long srId = (long)5;
        
        List<SpinReserveData> spinReserveDataList = 
        		spinReserveService.findSpinReserveDataBySrIdAndNoticeTypeAndNoticeTime(srId, NoticeType.UNLOAD, timestampStart, timestampEnd);
        
        List<SpinReserveData> newSpinReserveDataList = srSupRevenueService.getEnergy(srId, spinReserveDataList, AncillaryServiceType.sr);
  
        log.info(newSpinReserveDataList);
        
        if (newSpinReserveDataList != null && newSpinReserveDataList.size() > 0) {
            //Save spinReserveDataList
            spinReserveService.saveSpinReserveData(newSpinReserveDataList);
        }
        
        List<SpinReserveData> spinReserveDataList1 = 
        		spinReserveService.findSpinReserveDataBySrIdAndNoticeTypeAndNoticeTime(srId, NoticeType.UNLOAD, timestampStart, timestampEnd);
        
        List<SpinReserveData> newSpinReserveDataList1 = srSupRevenueService.getProformance(srId, spinReserveDataList1, AncillaryServiceType.sr);
  
        log.info(newSpinReserveDataList1);
        
        if (newSpinReserveDataList1 != null && newSpinReserveDataList1.size() > 0) {
            //Save spinReserveDataList
            spinReserveService.saveSpinReserveData(newSpinReserveDataList1);
        }
        
        List<SpinReserveData> spinReserveDataList2 = 
        		spinReserveService.findSpinReserveDataBySrIdAndNoticeTypeAndNoticeTime(srId, NoticeType.UNLOAD, timestampStart, timestampEnd);
        
        List<SpinReserveBid> newSpinReserveBid = srSupRevenueService.getHourlyServiceEnergy(srId, spinReserveDataList2, AncillaryServiceType.sr);
    
        if (newSpinReserveBid != null && newSpinReserveBid.size() > 0) {
            //Save newSpinReserveBid
            spinReserveService.addOrUpdateAll(srId, newSpinReserveBid);
        }
        
        List<SpinReserveData> spinReserveDataList3 = 
        		spinReserveService.findSpinReserveDataBySrIdAndNoticeTypeAndNoticeTime(srId, NoticeType.UNLOAD, timestampStart, timestampEnd);
        
        List<SpinReserveBid> newSpinReserveBid1 = srSupRevenueService.getServiceFactor(srId, spinReserveDataList3, AncillaryServiceType.sr);
    
        if (newSpinReserveBid1 != null && newSpinReserveBid1.size() > 0) {
            //Save newSpinReserveBid
            spinReserveService.addOrUpdateAll(srId, newSpinReserveBid1);
        }

    }
    
	@Test
	public void testAddEfficacyPriceToSpinReserveProfile() {


        Long srId = (long)5;
        Optional<SpinReserveProfile> srProfile = spinReserveService.findSpinReserveProfile(srId); 
        
        srProfile.get().setEfficacyPrice(new BigDecimal("100"));
        
        if (srProfile != null ) {
            //Save newSpinReserveBid
            //spinReserveService.updateSpinReserveProfile(srProfile.get());
        }
        
		
	}
	
    @Test
    public void testGetHourlyServiceEnergyBySridAndTime() {
        //計算時間
        try {
            timestampStart = Constants.TIMESTAMP_FORMAT.parse("2021-07-07 00:00:00");
            timestampEnd = Constants.TIMESTAMP_FORMAT.parse("2021-07-08 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Long srId = (long)5;
        
        List<SpinReserveData> spinReserveDataList2 = 
        		spinReserveService.findSpinReserveDataBySrIdAndNoticeTypeAndNoticeTime(srId, NoticeType.UNLOAD, timestampStart, timestampEnd);
        
        List<SpinReserveBid> newSpinReserveBid = srSupRevenueService.getHourlyServiceEnergy(srId, spinReserveDataList2, AncillaryServiceType.sr);
    
        if (newSpinReserveBid != null && newSpinReserveBid.size() > 0) {
            //Save newSpinReserveBid
            spinReserveService.addOrUpdateAll(srId, newSpinReserveBid);
        }
    }   

}
