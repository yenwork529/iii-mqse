package org.iii.esd.client.afc.performance;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.iii.esd.client.afc.AbstractServiceTest;
import org.iii.esd.mongo.service.AutomaticFrequencyControlLogService;
import org.iii.esd.mongo.service.AutomaticFrequencyControlMeasureService;
import org.iii.esd.mongo.service.AutomaticFrequencyControlService;
import org.iii.esd.mongo.service.UpdateService;
import org.iii.esd.mongo.vo.AfcLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.log4j.Log4j2;

@SpringBootTest(classes = { 
		AutomaticFrequencyControlService.class, 
		AutomaticFrequencyControlLogService.class,
		AutomaticFrequencyControlMeasureService.class, 
		UpdateService.class })
@EnableAutoConfiguration
@Log4j2
public class AfcLogControllerTest extends AbstractServiceTest {

	private Long afcId = 1L;
	
	private Long start = 1588089600000L;
	
	private Long end = 1588176000000L;
	
	@Autowired
	private AutomaticFrequencyControlService service;

	@Test
	public void testGetAfcLog() {
		try {
			List<AfcLog> afcLogList = service.getAfcLogList(afcId, start, end);		
			
			if (afcLogList.size()>0) {
				afcLogList.forEach(afcLog -> log.info(afcLog.toString()));				
			}			
			log.info("afcLogList.size()="+afcLogList.size());
		} catch (Exception ex) {
			log.error(ex.getMessage());
			fail();
		}		
	}
}
