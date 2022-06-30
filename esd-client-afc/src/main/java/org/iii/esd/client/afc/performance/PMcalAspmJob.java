package org.iii.esd.client.afc.performance;

import static org.iii.esd.mongo.enums.MeasureType.ASPM;
import static org.iii.esd.mongo.enums.MeasureType.SPM;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.iii.esd.afc.performance.Aspm;
import org.iii.esd.afc.performance.Result;
import org.iii.esd.api.response.ApiResponse;
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
public class PMcalAspmJob extends PMcalJob {

	//set this value from false to true to enable scheduled cron job
	@Value("${jobs.enabled:true}")
	private boolean isEnabled;
	
//	@Scheduled(cron="0 0 0 1 1 ?")
    public void doJob() throws InterruptedException, ExecutionException {
		if (isEnabled) {
			Long current = System.currentTimeMillis();
			Date yesterday = new Date(current-TimeUnit.DAYS.toMillis(1));
			//TODO: check this logic later
			// to regularly(every year) calculate aspm during the time of the past year
			doJob(AFC_ID, truncatedToYear(yesterday), current);
		}		
	}
	
	@PostMapping(URI_AFC_ASPM)  
	public ApiResponse doJob(@RequestParam long profileId, @RequestParam long start, @RequestParam long end) throws InterruptedException, ExecutionException {
		log.info("launch aspm job:" + Thread.currentThread().getName());
		
		int updated = 0;
		List<Aspm> aspmList = null;
		Optional<AutomaticFrequencyControlProfile> profile = afcService.findAutomaticFrequencyControlProfile(profileId);
		if (profile.isPresent()) {
			//profileId, start-from is gte(>=), end-to is le(<)
			List<AutomaticFrequencyControlMeasure> spmMeasureList = measureService.findAllByAfcIdAndTimeRangeAndType(profile.get().getId(), new Date(start), new Date(end), SPM.getShortName());
			log.info("afcMeasureList.size()="+spmMeasureList.size());

			Map<String, List<AutomaticFrequencyControlMeasure>> measureMap = spmMeasureList.stream().collect(Collectors.groupingBy(e -> truncated(e.getTimestamp())));
			aspmList = new ArrayList<Aspm>();
			List<AutomaticFrequencyControlMeasure> aspmMeasurelist = new ArrayList<AutomaticFrequencyControlMeasure>();

			for (Map.Entry<String, List<AutomaticFrequencyControlMeasure>> entry : measureMap.entrySet()) {
				try {
					List<BigDecimal> spmList = getSpmList(entry.getValue());
					
					LocalDateTime localDateTime = DatetimeUtils.toLocalDateTime(Long.parseLong(entry.getKey()));
					if (spmList.size()==0) {
						log.error("timestamp=" + localDateTime + ", spmList.size()="+spmList.size());
						break;
					}						
					aspmModule.setSpmList(spmList);
					BigDecimal aspm = aspmModule.calculate();
					updated++;

					log.info("timestamp=" + localDateTime + ", aspm=" + aspm + "(spmList.size()=" + spmList.size() + ")");
					
					aspmMeasurelist.add(getMeasure(profile.get(), DatetimeUtils.toDate(localDateTime), ASPM.getShortName(), aspm, spmList.size()));
					aspmList.add(new Aspm(localDateTime.toString(), aspm, spmList.size()));
				} catch (Exception ex) {
					log.error(ex.getMessage());
				}
			}

			if (aspmMeasurelist.size()>0) {
				saveMeasure(profile.get().getId(), aspmMeasurelist);				
			}
		}	
		Result result = new Result();
		result.setCount(updated);
		result.setAspmList(aspmList);
		return result;
	}
	
	private String truncated(Date time) {
		return String.valueOf(truncatedToYear(time));
	}

	private long truncatedToYear(Date time) {
		return DateUtils.truncate(time, Calendar.YEAR).getTime();
	}

	private List<BigDecimal> getSpmList(List<AutomaticFrequencyControlMeasure> measureList) throws IllegalArgumentException {
		List<BigDecimal> spmList = new ArrayList<BigDecimal>();
		Iterator<AutomaticFrequencyControlMeasure> iterator = measureList.iterator();
		while (iterator.hasNext()) {
			AutomaticFrequencyControlMeasure measure = iterator.next();
			if (measure.getTimestamp()==null || measure.getType()==null || measure.getValue()==null || measure.getCount()==null) {
				throw new IllegalArgumentException();
			} else {
				spmList.add(measure.getValue());
			}
		}
		return spmList;
	}
}
