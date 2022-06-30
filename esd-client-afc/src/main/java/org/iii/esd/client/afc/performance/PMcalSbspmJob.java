package org.iii.esd.client.afc.performance;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.iii.esd.afc.performance.Result;
import org.iii.esd.afc.performance.Sbspm;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.mongo.document.AutomaticFrequencyControlLog;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.utils.DatetimeUtils;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.log4j.Log4j2;

@Configuration
@EnableBatchProcessing
@EnableScheduling
@Log4j2
@RestController
public class PMcalSbspmJob extends PMcalJob {
	
	//set this value from false to true to enable scheduled cron job
	@Value("${jobs.enabled:true}")
	private boolean isEnabled;
	
//	@Scheduled(cron="* * * * * *")
    public void doJob() throws InterruptedException, ExecutionException {
		if (isEnabled) {
			Long current = System.currentTimeMillis();
			// to regularly(every second) calculate sbspm at current time before 1sec
			doJob(AFC_ID, current-TimeUnit.SECONDS.toMillis(2), current);
		}
	}
	
	/***
	 * To caculate AFC sbspm via rest controller.<br>
	 *   NOTICE:<br> 
	 *     The minimal interval between start and end should be in at least 2 seconds.<br>
	 *     For example, if you want to know what sbspm value it is at the time(t), then you should give start(t-1sec) and end(t+1sec).<br> 
	 * 	   eg. Give start: 1582905600000(t-1sec) and end: 1582905602000(t+1sec) => this function will calculate sbspm at 1582905601000(t).<br> 
	 * @param profileId
	 * @param start: unix time stamp in millisecond(t-1sec) 
	 * @param end: unix time stamp in millisecond(t+1sec)
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@PostMapping(URI_AFC_SBSPM)
	public ApiResponse doJob(@RequestParam long profileId, @RequestParam long start, @RequestParam long end) throws InterruptedException, ExecutionException {  
		log.info("launch sbspm job:" + Thread.currentThread().getName());
				
		int updated = 0;
		List<Sbspm> sbspmList = null;
		Optional<AutomaticFrequencyControlProfile> profile = afcService.findAutomaticFrequencyControlProfile(profileId);
		if (profile.isPresent()) {
			//profileId, start-from is gte(>=), end-to is le(<) 
			List<AutomaticFrequencyControlLog> logList = logService.findAllByAfcIdAndTimeRange(profile.get().getId(), new Date(start), new Date(end));
			log.info("afcLogList.size()="+logList.size());
			
			sbspmList = new ArrayList<Sbspm>();
			for(int i=0; i<logList.size(); i++) {
				int j=i+1;
				int k=j+1;
				if(k>=logList.size()) {
					break;
				}
				
				if(!isValidLog(logList.get(i)) || !isValidLog(logList.get(j))) {
					log.error("!!!Notice!!! it is not valid log [id:" + logList.get(i).getId() + " or " + logList.get(j).getId() + "]");
					break;
				}
				
				if(!isSecondBySecondData(logList.get(i).getTimestamp(), logList.get(j).getTimestamp())) {
					log.error("!!!Notice!!! it is not second by second data between log id:" + logList.get(i).getId() + " and log id:" + logList.get(j).getId());
					//break;
					continue;
				}
				
				Double[] frequencies = new Double[] {logList.get(i).getFrequency().doubleValue(), logList.get(j).getFrequency().doubleValue()};  						
				Double[] actualPowerRatios = new Double[] {logList.get(j).getEssPowerRatio().doubleValue(), logList.get(k).getEssPowerRatio().doubleValue()};				

				sbspmModule.setFrequencies(frequencies);
				sbspmModule.setActualPowerRatios(actualPowerRatios);

				LocalDateTime localDateTime = DatetimeUtils.toLocalDateTime(logList.get(j).getTimestamp());
				BigDecimal sbspm = sbspmModule.calculate();
				log.debug("timestamp=" + localDateTime + ", sbspm="+sbspm);
				
				// updating sbspm
				List<AutomaticFrequencyControlLog> newLogList = new ArrayList<AutomaticFrequencyControlLog>();
				logList.get(j).setSbspm(sbspm);
				logList.get(j).setUpdateTime(new Date(System.currentTimeMillis()));
				newLogList.add(logList.get(j));
				
				logService.addOrUpdateAll(profile.get().getId(), newLogList);
				updated++;
				sbspmList.add(new Sbspm(localDateTime.toString(), sbspm));
			}
		}	
		Result result = new Result();
		result.setCount(updated);
		result.setSbspmList(sbspmList);
		return result;
	}
	
	private boolean isValidLog(AutomaticFrequencyControlLog log) {
		if (log==null || log.getTimestamp()==null || log.getFrequency()==null || log.getEssPowerRatio()==null) {
			return false;
		}
		return true;
	}
	
	private boolean isSecondBySecondData(Date previous, Date subsequent) {
		long previousMillis = previous.getTime(); 
		long subsequentMillis = subsequent.getTime();		
		long diff = subsequentMillis - previousMillis; 

		return (diff==TimeUnit.SECONDS.toMillis(1)) ? true : false;
	}
}
