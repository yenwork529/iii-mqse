package org.iii.esd.nsysudata;


import java.math.BigDecimal;

import org.iii.esd.mongo.vo.data.setup.PVSetupData;
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
public class AppConfig {

	private Long fieldId;
	
	private SolarSetupData solarSetupData;

	private String url;
	
	private Device solarLoad;
	
	private Device mainLoad;
	
	private Jandi jandi;

	@Getter
	@Setter
    public static class Device {
		private String id;
		private String name;
		private String loadType;
		private String feed;
		private String channel;
    }

	@Getter
	@Setter
	public static class SolarSetupData {
		/**
		 * PV發電容量(kWp)
		 */
		private Integer pvCapacity;
		/**
		 * 單位採購成本($/kWp)
		 */
		private Integer unitCost;
		/**
		 * 維護費用(%)
		 */
		private BigDecimal maintenanceCost;
	}
	
	public PVSetupData getPVSetupData() {
		return PVSetupData.builder().
				pvCapacity(solarSetupData.getPvCapacity()).
				unitCost(solarSetupData.getUnitCost()).
				maintenanceCost(solarSetupData.getMaintenanceCost()).
				build();
	}
	
	@Getter
	@Setter
    public static class Jandi {
		private String uri;
		private String appName;
		private String color;
		private String title;
		private String description;
    }

}