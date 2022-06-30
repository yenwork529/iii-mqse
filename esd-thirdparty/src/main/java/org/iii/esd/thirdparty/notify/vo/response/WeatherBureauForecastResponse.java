package org.iii.esd.thirdparty.notify.vo.response;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 中央氣象局明日每3小時預報Response
 */
@Getter
@Setter
@NoArgsConstructor
public class WeatherBureauForecastResponse extends WeatherResponse {

    private String success;

    private Records records;

	@Getter
	@Setter
	public static class Records {
		private List<Locations> locations;

		@Getter
		@Setter
		public static class Locations {
			private String locationsName;
			private String dataid;
			private List<Location> location;
		}

		@Getter
		@Setter
		public static class Location {
			private String locationName;
			private String lat;
			private String lon;
			private List<WeatherElement> weatherElement;
		}

		@Getter
		@Setter
		public static class WeatherElement {
			private String elementName;
			private String description;
			private List<Time> time;
		}			

		@Getter
		@Setter
		public static class Time {
			private String dataTime;
			private List<ElementValue> elementValue;
		}

		@Getter
		@Setter
		public static class ElementValue {
			private String value;
			private String measures;
		}
	}

}