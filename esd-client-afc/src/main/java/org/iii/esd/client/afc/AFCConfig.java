package org.iii.esd.client.afc;

import java.math.BigDecimal;

import org.iii.esd.battery.config.Brand;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
//@Data
@EnableConfigurationProperties
@ConfigurationProperties(prefix="afc")
public class AFCConfig {

	private Long afcId;
	private String deviceId;
	private MeterConfig meterConfig;
	private BatteryConfig batteryConfig;
	private int scale;
//	private String ip;
//	private int port;
//	private int unit;	
//	private BigDecimal batteryKW;

	@Getter
	@Setter
    public static class MeterConfig {
		private MudbusConnect connect;
	    /*
		FR:	12288(0)~12289(1)	27136
		power:	12322(34)~12323(35) 27137
		Reactive Power:	12330(42)~12331(43)	27138
		Apparent Power:	12338(50)~12339(51)	27139
		Power Factor:	12346(58)~12347(59)	27140
		     */
	    private int pin;
	    private int count;
//	    private int ct1;
//	    private int ct2;
//	    private int pt1;
//	    private int pt2;
	    private BigDecimal frRatio;
	    private BigDecimal activePowerRatio;
	    private BigDecimal kVARRatio;
	    private BigDecimal kVARatio;
	    private BigDecimal powerFactorRatio;
    }
    
	@Getter
	@Setter
    public static class BatteryConfig {
		private MudbusConnect connect;
		private Brand brand;
		private BigDecimal batteryKW;
    }

	@Getter
	@Setter
    public static class MudbusConnect {
		private String ip;
		private int port;
		private int unit;
    }
 
}