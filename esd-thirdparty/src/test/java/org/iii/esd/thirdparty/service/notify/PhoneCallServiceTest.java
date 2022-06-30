package org.iii.esd.thirdparty.service.notify;

import static org.iii.esd.thirdparty.config.NotificationTypeEnum.AO_NOTICE;
import static org.iii.esd.thirdparty.config.NotificationTypeEnum.DO_ALERT_1;
import static org.iii.esd.thirdparty.config.NotificationTypeEnum.DO_EVENT_1;
import static org.iii.esd.thirdparty.config.NotificationTypeEnum.SYS_GW_1;
import static org.iii.esd.thirdparty.config.NotificationTypeEnum.SYS_VM_1;
import static org.iii.esd.thirdparty.config.NotificationTypeEnum.UNDEFINED;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URISyntaxException;

import org.iii.esd.thirdparty.AbstractServiceTest;
import org.iii.esd.thirdparty.config.PhoneConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import freemarker.template.Configuration;
import lombok.extern.log4j.Log4j2;

//@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PhoneCallService.class})
@ContextConfiguration(
	classes = {	
		PhoneConfig.class, 
		Configuration.class
	}
)
@Import({PhoneConfig.class})
@Log4j2
public class PhoneCallServiceTest extends AbstractServiceTest {

	private static final String[] callees = new String[]{"+886266073650", "+886979291856"};
	
	@Autowired
	private PhoneCallService service;
	
	@Test
	public void testMakeTwilioNoticeCall() {
		try {
			service.makeTwilioCall(callees, AO_NOTICE);
		} catch (URISyntaxException ex) {
			log.error(ex.getMessage());
			fail();
		}
	}

	@Test
	public void testMakeTwilioAlertCall() {
		try {
			service.makeTwilioCall(callees, DO_ALERT_1);
		} catch (URISyntaxException ex) {
			log.error(ex.getMessage());
			fail();
		}
	}
	
	@Test
	public void testMakeTwilioEventCall() {
		try {
			service.makeTwilioCall(callees, DO_EVENT_1);
		} catch (URISyntaxException ex) {
			log.error(ex.getMessage());
			fail();
		}
	}

	@Test
	public void testMakeTwilioUndefinedCall() {
		try {
			service.makeTwilioCall(callees, UNDEFINED);
		} catch (URISyntaxException ex) {
			log.error(ex.getMessage());
			fail();
		}
	}
	
	@Test
	public void testMakeTwilioSysVM1Call() {
		try {
			service.makeTwilioCall(callees, SYS_VM_1);
		} catch (URISyntaxException ex) {
			log.error(ex.getMessage());
			fail();
		}
	}	

	@Test
	public void testMakeTwilioSysGw1Call() {
		try {
			service.makeTwilioCall(callees, SYS_GW_1);
		} catch (URISyntaxException ex) {
			log.error(ex.getMessage());
			fail();
		}
	}	
}
