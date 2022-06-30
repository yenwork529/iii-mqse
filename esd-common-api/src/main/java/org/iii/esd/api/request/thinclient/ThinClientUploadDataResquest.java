package org.iii.esd.api.request.thinclient;

import java.util.List;

import org.iii.esd.api.MetaId;
import org.iii.esd.api.vo.DeviceReport;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.integrate.TxgElectricData;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ThinClientUploadDataResquest {

	private Long fieldId;

	private String fieldMetaId;

	/**
	 * 最近15分鐘資料(T1)
	 */
	@JsonProperty("csd")
	private ElectricData currentSectionData;
	/**
	 * 最近的即時資料(T99)
	 */
	@JsonProperty("rted")
	private ElectricData realTimeElectricData;

	/**
	 * 最近的裝置及時資料
	 */
	@JsonProperty("drd")
	private List<DeviceReport> deviceReportDatas;

	public ThinClientUploadDataResquest(Long fieldId) {
		this.fieldId = fieldId;
		this.fieldMetaId = MetaId.makeFieldId(fieldId);
	}

	public static ThinClientUploadDataResquest of(String metaId) {
		ThinClientUploadDataResquest y = new ThinClientUploadDataResquest();
		y.fieldMetaId = metaId;
		return y;
	}

	public static ThinClientUploadDataResquest of(String metaId, TxgElectricData tv) {
		ThinClientUploadDataResquest y = new ThinClientUploadDataResquest();
		y.fieldMetaId = metaId;
		return y;
	}

}