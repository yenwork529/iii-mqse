package org.iii.esd;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EsdIntegrationTestApplicationTests {

	@Autowired
	private EsdIntegrationTestApplication application;

	@Test
	void contextLoads() {
		Assertions.assertThat(application).isNotNull();
	}

}
