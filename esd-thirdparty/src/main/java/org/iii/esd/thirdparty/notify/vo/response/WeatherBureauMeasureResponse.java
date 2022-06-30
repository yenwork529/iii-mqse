package org.iii.esd.thirdparty.notify.vo.response;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 中央氣象局每小時量測Response
 */
@Getter
@Setter
@NoArgsConstructor
public class WeatherBureauMeasureResponse extends WeatherResponse {

    private String success;

    private Records records;

	@Getter
	@Setter
	public static class Records {
		private List<Location> location;
		
		@Getter
		@Setter
		public static class Location {
			private String lat;
			private String lon;
			private String locationName;
			private String stationId;
			private Time time;
			private List<WeatherElement> weatherElement;
			
			@Getter
			@Setter
			public static class Time {
				private String obsTime;
			}
			
			@Getter
			@Setter
			public static class WeatherElement {
				private String elementName;
				private String elementValue;
			}
			
		}
	}
}