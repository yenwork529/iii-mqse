package org.iii.esd.forecast.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.OptionalDouble;

import org.iii.esd.exception.IiiException;
import org.iii.esd.mongo.document.WeatherData;
import org.iii.esd.mongo.service.WeatherService;
import org.iii.esd.utils.DatetimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/***
 * 預測溫度跟歷史溫度目前資料關聯不明，
 * 先統一用預測的做一版，但是未來如果歷史部分有變更的話請記得修正
 * @author iii
 *
 */
@Service
public class TemperatureDataService {

	@Autowired
	WeatherService weatherService;

	/***
	 * 直接回傳資料，如果查不到由上層用try catch處理
	 * 
	 * @param stationId
	 * @param time
	 * @return
	 */
	public Double GetHistoryData(String stationId, Date time) {
		Date start = DatetimeUtils.add(time, Calendar.DAY_OF_YEAR, 0);
		Date end = DatetimeUtils.add(time, Calendar.DAY_OF_YEAR, 1);
		List<WeatherData> datas = weatherService.findActualDeviceStatisticsByDeviceIdAndTime(stationId, start, end);
		OptionalDouble result = datas.stream().mapToDouble(WeatherData::getTemperature).max();
		if (result.isPresent()) {
			return result.getAsDouble();
		} else {
			throw new IiiException("查無歷史溫度");
		}
	}
	/***
	 * 取得指定日期預測溫度
	 * @param stationId
	 * @param forecastday
	 * @return
	 */
	public Double GetForecastData(String stationId,Date forecastday) {
		Date start = DatetimeUtils.add(forecastday, Calendar.DAY_OF_YEAR, 0);
		Date end = DatetimeUtils.add(forecastday, Calendar.DAY_OF_YEAR, 1);
		List<WeatherData> datas = weatherService.findForecastDeviceStatisticsByDeviceIdAndTime(stationId, start, end);
		OptionalDouble result = datas.stream().mapToDouble(WeatherData::getTemperature).max();
		if (result.isPresent()) {
			return result.getAsDouble();
		} else {
			throw new IiiException("查無預測溫度");
		}
	}

}
