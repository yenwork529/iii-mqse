package org.iii.esd.client.afc.performance;

import static org.iii.esd.mongo.enums.MeasureType.SPM;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.iii.esd.afc.performance.Result;
import org.iii.esd.afc.performance.Spm;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.mongo.document.AutomaticFrequencyControlLog;
import org.iii.esd.mongo.document.AutomaticFrequencyControlMeasure;
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
public class PMcalSpmJob extends PMcalJob {

	//set this value from false to true to enable scheduled cron job
	@Value("${jobs.enabled:true}")
	private boolean isEnabled;
	
//	@Scheduled(cron="0 0/15 * * * ?")
    public void doJob() throws InterruptedException, ExecutionException {
		if (isEnabled) {
			Long current = System.currentTimeMillis();
			// to regularly(quarter of hour) calculate spm at time of the past 15 minutes
			doJob(AFC_ID, current-TimeUnit.MINUTES.toMillis(15), current);			
		}
	}
	
	@PostMapping(URI_AFC_SPM)
	public ApiResponse doJob(@RequestParam long profileId, @RequestParam long start, @RequestParam long end) throws InterruptedException, ExecutionException {  
		log.info("launch spm job:" + Thread.currentThread().getName());
		
		int updated = 0;
		List<Spm> spmList = null;
		Optional<AutomaticFrequencyControlProfile> profile = afcService.findAutomaticFrequencyControlProfile(profileId);
		if (profile.isPresent()) {
			//profileId, start-from is gte(>=), end-to is le(<)
			List<AutomaticFrequencyControlLog> logList = logService.findAllByAfcIdAndTimeRange(profile.get().getId(), new Date(start), new Date(end));
			log.info("afcLogList.size()="+logList.size());

			Map<String, List<AutomaticFrequencyControlLog>> logMap = logList.stream().collect(Collectors.groupingBy(e -> truncatedToQuarter(LocalDateTime.ofInstant(e.getTimestamp().toInstant(), ZoneId.of(DatetimeUtils.ZONE_ID)))));
			spmList = new ArrayList<Spm>();
			List<AutomaticFrequencyControlMeasure> measureList = new ArrayList<AutomaticFrequencyControlMeasure>();
			
			for (Map.Entry<String, List<AutomaticFrequencyControlLog>> entry : logMap.entrySet()) {
				try {
					List<BigDecimal> sbspmList = getSbspmList(entry.getValue());
					if (sbspmList.size()==0) {
						log.error("timestamp=" + entry.getKey() + ", sbspmList.size()="+sbspmList.size());
						break;
					}						
					spmModule.setSbspmList(sbspmList);
					BigDecimal spm = spmModule.calculate();
					updated++;

					LocalDateTime localDateTime = DatetimeUtils.toLocalDateTime(entry.getKey());
					spmList.add(new Spm(localDateTime.toString(), spm, sbspmList.size()));

					measureList.add(getMeasure(profile.get(), DatetimeUtils.toDate(localDateTime), SPM.getShortName(), spm, sbspmList.size()));
					log.info("timestamp=" + entry.getKey() + ", spm=" + spm + "(sbspmList.size()=" + sbspmList.size() + ")");
				} catch (Exception ex) {
					log.error(ex.getMessage());
				}
			}
			
			if (measureList.size()>0) {
				saveMeasure(profile.get().getId(), measureList);				
			}
		}	
		Result result = new Result();
		result.setCount(updated);
		result.setSpmList(spmList);
		return result;
	}	
	
	private List<BigDecimal> getSbspmList(List<AutomaticFrequencyControlLog> logList) throws IllegalArgumentException {
		List<BigDecimal> sbspmList = new ArrayList<BigDecimal>();
		Iterator<AutomaticFrequencyControlLog> iterator = logList.iterator();
		while (iterator.hasNext()) {
			AutomaticFrequencyControlLog log = iterator.next();
			if (log.getTimestamp()==null || log.getFrequency()==null || log.getEssPowerRatio()==null) {
				throw new IllegalArgumentException();
			} else {
				if (log.getSbspm()!=null)
					sbspmList.add(log.getSbspm());
			}
		}
		return sbspmList;
	}

	private String truncatedToQuarter(LocalDateTime time) {
	    LocalDateTime lastQuarter = time.truncatedTo(ChronoUnit.HOURS).plusMinutes(15 * (time.getMinute() / 15));
	    return lastQuarter.toString();
	}
}
