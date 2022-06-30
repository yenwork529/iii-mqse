package org.iii.esd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableAsync
public class EsdIntegrationTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(EsdIntegrationTestApplication.class, args);
	}

}
