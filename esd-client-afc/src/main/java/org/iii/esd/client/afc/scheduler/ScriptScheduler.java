package org.iii.esd.client.afc.scheduler;

import java.util.concurrent.ExecutionException;

import org.iii.esd.client.afc.AFCConfig;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.log4j.Log4j2;

@Configuration
@EnableBatchProcessing
@EnableScheduling
@Log4j2
public class ScriptScheduler {

    @Autowired
    private ReadProcessJob readProcessJob;

    @Autowired
    private UploadProcessJob uploadProcessJob;

    @Autowired
    private AFCConfig config;

    @Value("${control}")
    private String control;

    @Bean
    public AbstractControlProcessJob getControlProcessJob(){
    	if("test".equals(control)) {
    		return new TestSuitProcessJob();
    	}else {
    		return new ControlProcessJob();
    	}
    }

    Integer callcnt = 0;

	  @Scheduled(fixedRate = 100)
    public void readProcess() throws InterruptedException, ExecutionException {
      callcnt++;
      if((callcnt%30) == 0) {
        log.info("calling readProcess."+callcnt.toString());
      }
		  readProcessJob.run(config);
    }

	@Scheduled(cron="* * * * * *")
    public void controlProcess() throws InterruptedException, ExecutionException {
      log.info("control.Process");
		getControlProcessJob().run();
	}

	@Scheduled(cron="${uploadProcess_cron}")
    public void uploadProcess() throws InterruptedException, ExecutionException {
		uploadProcessJob.run();
	}	

//	private BigDecimal ratio() {
//		return new BigDecimal((config.getPt1()/config.getPt2()) * (config.getCt1()/config.getCt2()));
//	}

}