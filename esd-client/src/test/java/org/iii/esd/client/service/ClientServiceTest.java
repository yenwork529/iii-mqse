package org.iii.esd.client.service;

import static org.junit.jupiter.api.Assertions.fail;

import org.iii.esd.api.request.thinclient.ThinClientRegisterResquest;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.service.FieldProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;

import lombok.extern.log4j.Log4j2;

@EnableAutoConfiguration
@ContextConfiguration(
		classes = {
			ClientService.class,
			FieldProfileService.class,
		}
)
@Log4j2
class ClientServiceTest extends AbstractServiceTest {
	
    @Autowired
    private FieldProfileService fieldProfileService;
	
	@Autowired
	private ClientService clientService;
	
	//@Value("${fieldId}")
	private Long fieldId=1l;

	@Test
	void testCallRegister() {
		FieldProfile fieldProfile = fieldProfileService.find(fieldId).get();
		EnableStatus tcEnable = fieldProfile.getTcEnable();
		
		log.info(fieldProfile.getTcIp());
		log.info(fieldProfile.getId());
		
		log.info(new ThinClientRegisterResquest(fieldProfile).toString());
		
		// 還沒同步才需要啟用
		if(!EnableStatus.enable.equals(tcEnable)) {
			// 1.Register
			clientService.callRegister(new ThinClientRegisterResquest(fieldProfile));
		}
	}

	@Test
	void testSyncField() {
		fail("Not yet implemented");
	}

	@Test
	void testUploadData() {
		fail("Not yet implemented");
	}

	@Test
	void testSchedule() {
		fail("Not yet implemented");
	}

	@Test
	void testReschedule() {
		fail("Not yet implemented");
	}

	@Test
	void testFix() {
		fail("Not yet implemented");
	}

}
