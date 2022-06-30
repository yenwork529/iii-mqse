package org.iii.esd.api.request.thinclient;

import java.util.List;

import org.iii.esd.mongo.document.ElectricData;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
// @AllArgsConstructor
public class ThinClientFixDataResquest {
	
	private Long fieldId;
	private String fieldMetaId;

	private List<ElectricData> list;

	public ThinClientFixDataResquest(Long fieldId, List<ElectricData> dataList){
		this.fieldId = fieldId;
		this.list = dataList;
	}

}