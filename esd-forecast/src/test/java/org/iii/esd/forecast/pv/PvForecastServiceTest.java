package org.iii.esd.forecast.pv;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Date;
import java.util.List;

import org.iii.esd.forecast.service.IlluminanceDataService;
import org.iii.esd.mongo.config.MongoDBConfig;
import org.iii.esd.mongo.document.KwEstimation;
import org.iii.esd.mongo.service.ElectricDataService;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.mongo.service.KwEstimationService;
import org.iii.esd.mongo.service.UpdateService;
import org.iii.esd.mongo.service.WeatherService;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;

import lombok.extern.log4j.Log4j2;

@TestMethodOrder(OrderAnnotation.class)
@ContextConfiguration(classes = MongoDBConfig.class)
@ConfigurationProperties(prefix = "test")
@PropertySource({ "classpath:application.yml", "classpath:application-local.yml", })
@SpringBootTest(classes = { PvForecastService.class, ElectricDataService.class, FieldProfileService.class,
		WeatherService.class, UpdateService.class,IlluminanceDataService.class,KwEstimationService.class })
@EnableAutoConfiguration
@Log4j2
public class PvForecastServiceTest {

	@Autowired
	PvForecastService pvService;

	Long profileId = 999L;
	/***
	 * 缺少2017年度的溫度跟照度資料，所以依據目前有的資料的基礎，假塞2017年度全年資料
	 */
	@Test
	@Order(1)
	void TestEveryDayLoop() {
		pvService.SolarDataTraning(profileId);
	}

	/**
	 * 無照度資料測試
	 */
	@SuppressWarnings("deprecation")
	@Test
	void GetPVKwEstimation() {
		List<KwEstimation> result = pvService.GetPVKwEstimation(profileId, new Date("2017/05/03"));
		if(result.size()!=96) {
			fail();
		}
		result.forEach(a->{
			log.info("FieldId:{},Category:{},Group:{},Seconds:{},Value:{}",a.getFieldId(),a.getCategory(),a.getGroup(),a.getSeconds(),a.getValue());
		});
	}
	/**
	 * 有照度資料測試
	 */
	@SuppressWarnings("deprecation")
	@Test
	void GetPVKwEstimation2() {
		
		List<KwEstimation> result = pvService.GetPVKwEstimation(profileId, new Date("2017/09/10"));
		if(result.size()!=96) {
			fail();
		}
		result.forEach(a->{
			log.info("FieldId:{},Category:{},Group:{},Seconds:{},Value:{}",a.getFieldId(),a.getCategory(),a.getGroup(),a.getSeconds(),a.getValue());
		});
	}
}
