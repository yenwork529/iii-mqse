package org.iii.esd.forecast.service;

import java.util.Date;
import java.util.List;

import org.iii.esd.exception.IiiException;
import org.iii.esd.mongo.document.WeatherData;
import org.iii.esd.mongo.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 預測溫度跟歷史溫度目前資料關聯不明， 先統一用預測的做一版，但是未來如果歷史部分有變更的話請記得修正
 */
@Service
public class IlluminanceDataService {

	@Autowired
	WeatherService weatherService;

	private Date[] getLuxInterval(Date date) {
		long millisecond = date.getTime();
		return new Date[] {
//			new Date(millisecond + (11*60+15)*1000),	//	11:15
//			new Date(millisecond + (14*60)*1000)		//	14:00
			new Date(millisecond + (11*60*60+15*60)*1000),	//	11:15
			new Date(millisecond + (14*60*60)*1000)		//	14:00
		};
	}

	/***
	 * 計算歷史資料期間內PV平均照度，假如找不到資料或異常就當作0 
	 * 尋找當天天氣所有照度數值並平均，照度資料只看11:15~14:00<br/>
	 * @param stationId
	 * @param date
	 */
	public Double getHistoryAverageLux(String stationId, Date date) {
		try {
			// double DayLux = pdb.TB_Min_Value
			// .Where(a => luxDeviceID.Contains(a.DEVICE_ID) && a.DATE_TIME >= s_lux &&
			// a.DATE_TIME <= e_lux && a.LUX >= 0 && a.LUX <= 1000)
			// .Average(a => a.LUX).GetValueOrDefault();
			List<WeatherData> luxs = weatherService.findActualDeviceStatisticsByDeviceIdAndTime(stationId,
					getLuxInterval(date)[0], getLuxInterval(date)[1]);
			return luxs.stream().filter(a -> a.getIlluminance() >= 0 && a.getIlluminance() <= 1000)
					.mapToDouble(WeatherData::getIlluminance).average().getAsDouble();
		} catch (Throwable ex) {
			throw new IiiException("查無歷史照度");
		}

	}

	/**
	 * 取得未來預測日平均照度
	 * 照度資料只看11:15~14:00 <br/>
	 * @param stationId
	 * @param date
	 */
	public Double GetForecastDayAverageLux(String stationId, Date date) {
		try {
			Date[] thisdate = getLuxInterval(date);
			List<WeatherData> luxs = weatherService.findForecastDeviceStatisticsByDeviceIdAndTime(stationId,
					getLuxInterval(date)[0], getLuxInterval(date)[1]);
			return luxs.stream().filter(a -> a.getIlluminance() >= 0 && a.getIlluminance() <= 1000)
					.mapToDouble(WeatherData::getIlluminance).average().getAsDouble();
		} catch (Throwable ex) {
			throw new IiiException("查無預測照度");
		}
	}

}