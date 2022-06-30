package org.iii.esd.forecast.domain;

import java.util.Date;
import java.util.List;

import org.iii.esd.mongo.document.ForecastSource;
import org.iii.esd.mongo.document.KwEstimation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ElectricDataKmeans {

	
	Date recordDay;
	Long profileId;
	int category;
	int group;
	/**
	 * PV 訓練時當作照度數值<br>
	 * 負載訓練時 當作溫度數值
	 */
	double averageValue;
	boolean rightGroup = false;
	List<KwEstimation> electricDatas;
	/**
	 * 將ForecastSource轉成ElectricDataKmeans
	 * @param source
	 * @return
	 */
	public static ElectricDataKmeans TransformFrom(ForecastSource source) {
		ElectricDataKmeans edks = new ElectricDataKmeans();
		edks.setCategory(source.getCategory());
		edks.setGroup(source.getGroup());
		edks.setProfileId(source.getFieldId());
		edks.setRecordDay(source.getTime());
		edks.setAverageValue(source.getTemperature());
		return edks;
	}
}
