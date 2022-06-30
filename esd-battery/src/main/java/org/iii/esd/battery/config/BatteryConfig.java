package org.iii.esd.battery.config;

import java.util.HashMap;
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
public class BatteryConfig {

	private final Map<String, Config> aec = new HashMap<>();
	
	private final Map<String, Config> chem = new HashMap<>();
	
	private final Map<String, Config> joseph = new HashMap<>();
	
	private final Map<String, Config> sungrow = new HashMap<>();
	
	private Basic aecBasic;
	
	private Basic chemBasic;
	
	private Basic josephBasic;
	
	private Basic sungrowBasic;

	@Getter
	@Setter
    public static class Config {
		private Integer pinNo;
		private Boolean isSigned;
    	private Integer lowPinNo;
    	private Double charge;
    	private Double discharge;
    	private Integer standby; 
    	private Integer movePointLeft;
    	private Byte rw;
    }	

	@Getter
	@Setter
    public static class Basic {
		private Integer chargeMaxCurrent;
    	private Integer dischargeMaxCurrent;    	
    	private Integer socMin;
    }	

}