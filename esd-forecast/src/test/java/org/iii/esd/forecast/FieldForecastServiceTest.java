package org.iii.esd.forecast;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Date;
import java.util.List;

import org.iii.esd.enums.DataType;
import org.iii.esd.forecast.load.LoadForecastService;
import org.iii.esd.forecast.pv.PvForecastService;
import org.iii.esd.forecast.service.FieldForecastService;
import org.iii.esd.forecast.service.IlluminanceDataService;
import org.iii.esd.forecast.service.TemperatureDataService;
import org.iii.esd.mongo.config.MongoDBConfig;
import org.iii.esd.mongo.document.ElectricData;
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
@SuppressWarnings("deprecation")
@ContextConfiguration(classes = MongoDBConfig.class)
@ConfigurationProperties(prefix = "test")
@PropertySource({ "classpath:application.yml", "classpath:application-local.yml", })
@SpringBootTest(classes = { LoadForecastService.class, ElectricDataService.class, FieldProfileService.class,
		PvForecastService.class, WeatherService.class, UpdateService.class, TemperatureDataService.class,
		KwEstimationService.class, FieldForecastService.class, IlluminanceDataService.class })
@EnableAutoConfiguration
@Log4j2
public class FieldForecastServiceTest {

	@Autowired
	FieldForecastService service;

	Long fieldId = 999L;

	/***
	 * ??????????????????
	 */
	@Test
	@Order(1)
	public void Training() {
		service.Training(fieldId);
	}

	/**
	 * ????????????????????????
	 */
	@Test
	public void CreateFullForecastDatas() {
		DataType type = DataType.T10;
		List<ElectricData> datas = service.GetFieldFullForecastElectricData(fieldId, type, new Date("2018/06/05"));
		datas.forEach(a -> {
			if (!a.getFieldProfile().getId().equals(fieldId)) {
				fail("??????????????????ID??????");
			}
			if (a.getDataType() != type) {
				fail("????????????DataType??????");
			}
			log.trace(a);
		});
		// ????????????
		for (int i = 1; i < 96; i++) {
			ElectricData b = datas.get(i);
			ElectricData a = datas.get(i - 1);
			if (a.getTime().compareTo(b.getTime()) >= 0) {
				fail("??????????????????????????????");
			}
		}
		// ????????????????????????
		assertEquals(datas.size(), 96);
	}

	public void SaveFieldFullForecastElectricDataInDatabase() {
		DataType type = DataType.T10;
		service.SaveFieldFullForecastElectricDataInDatabase(fieldId, type, new Date("2018/06/05"));
	}

	/**
	 * ????????????????????????
	 */
	@Test
	public void CorrectionInDatabase() {
		DataType type = DataType.T10;
		try {
			
			service.SaveFieldFullForecastElectricDataInDatabase(fieldId, type, new Date("2017/05/06"));
		} catch (Throwable ex) {

		}
		service.correctionInDatabase(fieldId, new Date("2017/05/06 15:15"), DataType.T1, type);
	}

}
