package org.iii.esd.thirdparty.service.weather;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.iii.esd.Constants;
import org.iii.esd.enums.ResponseStatus;
import org.iii.esd.thirdparty.config.Config;
import org.iii.esd.thirdparty.config.Config.Param;
import org.iii.esd.thirdparty.config.Config.WeatherBureau;
import org.iii.esd.thirdparty.notify.vo.response.WeatherBureauForecastResponse;
import org.iii.esd.thirdparty.notify.vo.response.WeatherBureauForecastResponse.Records.Location;
import org.iii.esd.thirdparty.notify.vo.response.WeatherBureauForecastResponse.Records.Time;
import org.iii.esd.thirdparty.notify.vo.response.WeatherBureauMeasureResponse;
import org.iii.esd.thirdparty.service.HttpService;
import org.iii.esd.thirdparty.weather.WeatherVO;
import org.iii.esd.utils.DatetimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
https://opendata.cwb.gov.tw/api/v1/rest/datastore/O-A0003-001?Authorization=CWB-973DBD86-AE7F-4B31-A93B-487CE21DD7A9&stationId=466920,467050,467440,467490,466900,466990&elementName=TEMP,HUMD,H_UVI
https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-D0047-093?Authorization=CWB-973DBD86-AE7F-4B31-A93B-487CE21DD7A9&locationId=F-D0047-061,F-D0047-005,F-D0047-065,F-D0047-069,F-D0047-073&locationName=松山區,淡水區,鼓山區,中區,新屋區&elementName=T,RH&timeTo=2020-01-10T01:00:00
https://e-service.cwb.gov.tw/wdps/obs/state.htm
https://opendata.cwb.gov.tw/opendatadoc/DIV2/A0003-001.pdf
 */
@Service
public class WeatherBureauService {
	
	@Autowired
	private Config config;
	
	@Autowired
	private HttpService httpService;

	public WeatherBureauMeasureResponse sendMeasureMessage() {
		WeatherBureau weatherBureau = config.getWeatherBureau();
		Param param = weatherBureau.getMeasure();
		return httpService.formGet(
				MessageFormat.format(weatherBureau.getUrl().concat(param.getQparam()), 
						weatherBureau.getToken(), 
						String.join(",", config.getStation().keySet()), 
						String.join(",", param.getElements())
				), WeatherBureauMeasureResponse.class);
	}	
	
	public List<WeatherVO> getMeasureWeatherList() {
		List<WeatherVO> list = new ArrayList<>();
		WeatherBureauMeasureResponse reponse = sendMeasureMessage();
		if(ResponseStatus.ok.equals(reponse.getStatus())) {
			reponse.getRecords().getLocation().forEach(l->list.add(new WeatherVO(l)));
		}
		return list;
	}

	public WeatherBureauForecastResponse sendForecastMessage() {
		WeatherBureau weatherBureau = config.getWeatherBureau();
		Param param =weatherBureau.getForecast();
		Map<String,String> map = config.getLocation();
		
		return httpService.formGet(
				MessageFormat.format(weatherBureau.getUrl().concat(param.getQparam()), 
						weatherBureau.getToken(), 
						String.join(",", map.keySet()), 
						String.join(",", map.values().stream().map(s-> s.split("\\|")[0]).collect(Collectors.toList())),
						String.join(",", param.getElements()),
						Constants.ISO8601_FORMAT2.format(DatetimeUtils.add(DatetimeUtils.add(DatetimeUtils.getFirstHourOfDay(new Date()), Calendar.DATE, 2), Calendar.HOUR, 1))
				), WeatherBureauForecastResponse.class);
	}	
	
	public List<WeatherVO> getForecastWeatherList() {
		List<WeatherVO> list = new ArrayList<>();
		WeatherBureauForecastResponse reponse = sendForecastMessage();
		if(ResponseStatus.ok.equals(reponse.getStatus())) {
			reponse.getRecords().getLocations().forEach(ls->ls.getLocation().forEach(l->list.addAll(transform(l))));
		}
		return checkData(mergeList(list));
	}

	private List<WeatherVO> transform(Location location) {
		String stationId = getStationId(location.getLocationName());
		List<WeatherVO> list = new ArrayList<>();
		location.getWeatherElement().forEach(weatherElement->{
			String elementName = weatherElement.getElementName();
			List<Time> timeList = weatherElement.getTime();
			timeList.forEach(time->list.add(new WeatherVO(
					stationId,
					DatetimeUtils.parseDate(time.getDataTime(), Constants.TIMESTAMP_FORMAT),
					"T".equals(elementName)?new Double(time.getElementValue().get(0).getValue()):null,
					"RH".equals(elementName)?new Double(time.getElementValue().get(0).getValue()):null,
					null,
					null
			)));
		});
		return list;
	}

	private String getStationId(String location) {
		return config.getLocation().values().stream().collect(Collectors.toMap( s->s.split("\\|")[0], s-> s.split("\\|")[1])).get(location);
	}
	
	private List<WeatherVO> checkData(List<WeatherVO> list) {
		return list.stream().filter(w->w.getTime().after(new Date())).collect(Collectors.toList());
	}
	
	/**
	 * 整理相同StationId和Time的資料
	 * @param list
	 */
	public static List<WeatherVO> mergeList(List<WeatherVO> list) {
		Map<String, List<WeatherVO>> map = 
		list.stream().collect(
				Collectors.groupingBy(w-> w.getStationId().concat(Constants.DATETIME_FORMAT.format(w.getTime())))
		);
		return map.values().stream().
				map(WeatherBureauService::merge). 
                sorted(Comparator.comparing(WeatherVO::getStationId).thenComparing(WeatherVO::getTime)).                
                collect(Collectors.toList());		
	}

	/**
	 * 相同 StationId和Time的資料合併為一筆資料
	 * @param list
	 */
	private static WeatherVO merge(List<WeatherVO> list) {
    	WeatherVO w = list.get(0);
    	for (WeatherVO weatherVO : list) {
    		w.merge(weatherVO);
		}
        return w;
    }	

}