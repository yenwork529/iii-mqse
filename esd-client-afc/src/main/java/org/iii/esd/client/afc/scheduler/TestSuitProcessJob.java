package org.iii.esd.client.afc.scheduler;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.beanutils.PropertyUtils;
import org.iii.esd.Constants;
import org.iii.esd.annotation.LogExecutionTime;
import org.iii.esd.api.vo.ModbusMeter;
import org.iii.esd.client.afc.AFCConfig;
import org.iii.esd.client.afc.performance.PMcalSbspmJob;
import org.iii.esd.client.afc.performance.PMcalSpmJob;
import org.iii.esd.thirdparty.config.TestSuits;
import org.iii.esd.thirdparty.config.TestSuits.TestSuit;
import org.iii.esd.thirdparty.service.HttpService;
import org.iii.esd.utils.DatetimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component("test")
@ConfigurationProperties
@Log4j2
public class TestSuitProcessJob extends AbstractControlProcessJob {

	private int index = 0;
	
	@Autowired
	private TestSuits testSuits;

	// 是否讀為上一次計算好充放電比例
	@Value("${essSimulate}")
	private Boolean essSimulate;
	
	@Value("${testSuitList}")
	private List<String> testSuitList;
	
	@Value("${initSoc}")
	private int initSoc;
	
	private BigDecimal activePower = BigDecimal.ZERO;
	
	private long firstSec = 0;
	
    @Autowired
    private AFCConfig config;
    
	@Autowired
	private PMcalSbspmJob pMcalSbspmJob;
	
	@Autowired
	private PMcalSpmJob pMcalSpmJob;

	@Autowired
	private HttpService httpService;
	
	private Date lastfinishSec = null;

	private List<BigDecimal> getData() {
		List<BigDecimal> list = new ArrayList<>();
		
		if("t4_1".equals(testSuitList.get(0))) {
			list = Arrays.asList(new BigDecimal[60*180+3]);
		}else {
			for (String tsList : testSuitList) {
				try {
					list.addAll(((TestSuit)PropertyUtils.getProperty(testSuits.getSuit(), tsList)).getFrequencies());
				} catch (Exception e) {
					log.error(e.toString());
				}
			}			
		}
    	return list;
	}

	@LogExecutionTime
	@Async("controlExecutor")
	public Future<Void> run() throws InterruptedException {
		Thread.sleep(10l);
		Date thisSec = DatetimeUtils.truncated(new Date(), Calendar.SECOND);
		//log.info(index +" "+ getData().size());
		if(index < getData().size()) {
			long id = genId();
			Thread.sleep(500l);
			ModbusMeter currentModbusMeter = getModbusMeter(id);
			if (essSimulate) {
				currentModbusMeter.setActivePower(activePower);
			}
			// 除了實際併聯測試外才要讀設定檔頻率
			if(!"t4_1".equals(testSuitList.get(0))) {
				currentModbusMeter.setActualFrequency(getData().get(index));				
			}
			//log.info(id+" " + Constants.ISO8601_FORMAT3.format(thisSec));
			BigDecimal essPowerRatio = BigDecimal.ZERO;
			if(index == 0) {
				firstSec = thisSec.getTime();
			}else {
				long diff = ChronoUnit.SECONDS.between(lastfinishSec.toInstant(), thisSec.toInstant()); 
				for (long i = diff-1; i >=0 ; i--) {
					Date time = DatetimeUtils.add(thisSec, Calendar.SECOND, -(int)i);
					if(i>0) {
						log.warn("this time:{} is fixed. {}/{}", time,i,diff);
					}
					essPowerRatio = writeLog(time, currentModbusMeter);
				}
			}
			activePower = controlEss(getModbusMeter((id + 90) % 100), currentModbusMeter, essPowerRatio);

		} else if (index == getData().size()) {
			try {
				Long endSec = thisSec.getTime()+1000;
				pMcalSbspmJob.doJob(config.getAfcId(),firstSec,endSec);
				log.info("sbspm calculate finished.");
				pMcalSpmJob.doJob(config.getAfcId(),firstSec,endSec);
				log.info("spm calculate finished.");
				System.out.printf("profileId:%d%nstart:%d%nend:%d%n",config.getAfcId(),firstSec,endSec);
				httpService.saveFile(String.format("http://localhost:58015/afc/pm/log?profileId=%d&start=%d&end=%d", 
						new Object[]{config.getAfcId(),firstSec,endSec}), 
						HttpMethod.POST, "C:/afc/", "testSuit.csv");
			} catch (Exception e) {
				log.error(e.getMessage());
			} finally {
				standby();				
				log.info("TestSuit is finished.");
			}
		}
		index++;
		lastfinishSec = thisSec;
		return new AsyncResult<Void>(null);
	}
	
	protected double readSOC() {
		return initSoc;
	}
	

}