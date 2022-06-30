package org.iii.esd.thirdparty.service.weather;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.iii.esd.thirdparty.AbstractServiceTest;
import org.iii.esd.thirdparty.notify.vo.response.WeatherBureauForecastResponse;
import org.iii.esd.thirdparty.notify.vo.response.WeatherBureauMeasureResponse;
import org.iii.esd.thirdparty.service.HttpService;
import org.iii.esd.thirdparty.weather.WeatherVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@ContextConfiguration(
		classes = {
			HttpService.class,
			WeatherBureauService.class	
		}
)
@Log4j2
class WeatherBureauServiceTest extends AbstractServiceTest {

	@Autowired
	private WeatherBureauService service;

	@Test
	void testSendMeasureMessage() {
		WeatherBureauMeasureResponse response = service.sendMeasureMessage();
		log.info(response.getSuccess());
	}

	@Test
	void testGetMeasureWeatherList() {
		List<WeatherVO> list = service.getMeasureWeatherList();
		assertThat(list.size(), greaterThan(0));
		list.forEach(w->log.info(w));
	}
	
	@Test
	void testSendForecastMessage() {
		WeatherBureauForecastResponse response = service.sendForecastMessage();
		log.info(response.getSuccess());
	}
	
	@Test
	void testGetForecastWeatherList() {
		List<WeatherVO> list = service.getForecastWeatherList();
		assertThat(list.size(), greaterThan(0));
		list.forEach(w->log.info(w));	
	}

}