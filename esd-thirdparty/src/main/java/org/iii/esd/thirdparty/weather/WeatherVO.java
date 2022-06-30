package org.iii.esd.thirdparty.weather;

import java.util.Date;
import java.util.List;

import org.iii.esd.Constants;
import org.iii.esd.thirdparty.notify.vo.response.WeatherBureauMeasureResponse.Records.Location;
import org.iii.esd.thirdparty.notify.vo.response.WeatherBureauMeasureResponse.Records.Location.WeatherElement;
import org.iii.esd.utils.DatetimeUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WeatherVO {
	
	private String stationId;
	
	private Date time;

	private Double temperature;
	
	private Double humidity;
	
	private Double uv;
	
	private Double illuminance;

	public WeatherVO(Location location) {
		this.stationId = location.getStationId();
		this.time = DatetimeUtils.parseDate(location.getTime().getObsTime(), Constants.TIMESTAMP_FORMAT);
		List<WeatherElement> weatherElement = location.getWeatherElement();
		weatherElement.forEach(we->{
			if("TEMP".equals(we.getElementName())) {
				this.temperature = new Double(we.getElementValue());
			}else if("HUMD".equals(we.getElementName())) {
				this.humidity = new Double(we.getElementValue())*100;
			}else if("H_UVI".equals(we.getElementName())) {
				this.uv = new Double(we.getElementValue());
			}
		});
	}
	
	public WeatherVO merge(WeatherVO other) {
		if (stationId != null && stationId.equals(other.getStationId()) && 
				time != null && time.equals(other.getTime())) {
			this.temperature = other.temperature == null ? this.temperature : other.temperature;
			this.humidity = other.humidity == null ? this.humidity : other.humidity;
			this.uv = other.uv == null ? this.uv : other.uv;
			this.illuminance = other.illuminance == null ? this.illuminance : other.illuminance;
		}
		return this;
	}

}