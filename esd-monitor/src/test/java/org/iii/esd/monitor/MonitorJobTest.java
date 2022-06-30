package org.iii.esd.monitor;

import lombok.extern.log4j.Log4j2;
import org.iii.esd.monitor.scheduler.MonitorJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@EnableAutoConfiguration
@SpringBootTest
@Log4j2
public class MonitorJobTest {

    @Autowired
    private MonitorJob job;

    @Test
    public void testMonitor() {
        job.run();
    }
}
