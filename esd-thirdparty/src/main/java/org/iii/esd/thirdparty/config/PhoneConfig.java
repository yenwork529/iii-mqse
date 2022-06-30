package org.iii.esd.thirdparty.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "spring.phone")
public class PhoneConfig {

	private TwilioCloud twilioCloud;

	@Data
	public static class TwilioCloud {

		@Value("${account-sid}")
		private String accountSid;
		
		@Value("${auth-token}")
		private String authToken;
		
		// caller phone number
		private String from;
		
		// mode of getting an audio file
		private String mode;
		
		private String locator;
		
		private String[] uri;
		
		@Value("${application-sid}")
		private String[] applicationSid;
	}
}
