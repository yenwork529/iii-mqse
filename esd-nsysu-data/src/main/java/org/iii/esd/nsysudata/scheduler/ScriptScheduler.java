package org.iii.esd.nsysudata.scheduler;

import org.iii.esd.nsysudata.AppConfig;
import org.iii.esd.nsysudata.service.MdService;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableBatchProcessing
@EnableScheduling
public class ScriptScheduler {

	@Value("${M1}")
	private String mainDeviceId;

	@Value("${M2}")
	private String pvDeviceId;

	@Autowired
	private AppConfig appConfig;
	
	@Autowired
	private MdService mdService;
	
	/**
	 * 固定六個值：秒(0-59) 分(0-59) 時(0-23) 日(1-31) 月(1-12) 週(1,日-7,六)
	 * @throws Exception
	 */
	@Scheduled(cron="5 * * * * *")
	public void launchJob() throws Exception {
		mdService.saveRealTimeData(mainDeviceId, appConfig.getMainLoad());
		mdService.saveRealTimeData(pvDeviceId, appConfig.getSolarLoad());
		// FIXME 電池資料待補
	}

}