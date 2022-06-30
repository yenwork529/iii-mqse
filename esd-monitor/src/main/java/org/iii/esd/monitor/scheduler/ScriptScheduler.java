package org.iii.esd.monitor.scheduler;

import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableBatchProcessing
@EnableScheduling
@Log4j2
public class ScriptScheduler {

    @Autowired
    private MonitorJob job;

    @Scheduled(fixedRate = 60 * 1000)
    public void serverProcess() {
        job.run();
    }
}