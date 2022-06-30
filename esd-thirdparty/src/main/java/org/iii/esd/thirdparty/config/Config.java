package org.iii.esd.thirdparty.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix="config")
public class Config {
	
	private Jandi jandi;
	
	private WeatherBureau weatherBureau;
	
	private Soda soda;

	private Map<String,String> station;
	
	private Map<String,String> location;
	
	@Getter
	@Setter
    public static class Jandi {
		private String url;
    }
	
	@Getter
	@Setter
    public static class WeatherBureau {
		private String url;
		private String token;
		private Param measure;
		private Param forecast;
    }

	@Getter
	@Setter
    public static class Param {
		private String qparam;
		private String dataid;
		private List<String> elements;
    }

	@Getter
	@Setter
    public static class Soda {
		private String urlSession;
		private String url;
		private String actually;
		private String forecast;
    }

}