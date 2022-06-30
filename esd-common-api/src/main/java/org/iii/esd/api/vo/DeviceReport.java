package org.iii.esd.api.vo;

import java.util.Date;

import org.iii.esd.mongo.vo.data.measure.MeasureData;

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
public class DeviceReport {
	
	private String id;
	
	private MeasureData measureData;
	
	private Date reportTime;

}