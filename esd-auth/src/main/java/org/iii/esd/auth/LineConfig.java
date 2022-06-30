package org.iii.esd.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties("line")
public class LineConfig {

	private final Notify notify = new Notify();

	@Data
    public static class Notify {
		private String clientId;
		private String clientSecret;
		private String token;
    }

}