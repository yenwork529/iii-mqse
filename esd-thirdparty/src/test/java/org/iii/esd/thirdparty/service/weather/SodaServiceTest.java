package org.iii.esd.thirdparty.service.weather;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.iii.esd.enums.WeatherType;
import org.iii.esd.thirdparty.AbstractServiceTest;
import org.iii.esd.thirdparty.service.HttpService;
import org.iii.esd.thirdparty.weather.WeatherVO;
import org.iii.esd.utils.DatetimeUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@ContextConfiguration(
		classes = {
			HttpService.class,
			SodaService.class	
		}
)
@Log4j2
class SodaServiceTest extends AbstractServiceTest {

	@Autowired
	private SodaService service;

	@Test
	void testSendMeasureMessage() {
		// 實際(注意只能抓取上上個月前的資料)
		Date start = DatetimeUtils.add(DatetimeUtils.truncated(new Date(), Calendar.DATE),Calendar.MONTH,-2);
		Date end = DatetimeUtils.add(start, Calendar.DATE, 1);
		List<WeatherVO> list = service.sendMessage(WeatherType.actually, start, end, "C:/weather");
		list.forEach(w->log.info(w));
	}	
	
	@Test
	void testSendForecastMessage() {
		// 預測
		Date start = DatetimeUtils.truncated(new Date(), Calendar.HOUR);
		Date end = DatetimeUtils.add(DatetimeUtils.truncated(start, Calendar.DATE), Calendar.DATE, 2);
		List<WeatherVO> list = service.sendMessage(WeatherType.forecast, start, end, "D:/weather",3);
		list.forEach(w->log.info(w));
	}

}
