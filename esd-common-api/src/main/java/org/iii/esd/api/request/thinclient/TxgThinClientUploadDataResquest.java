package org.iii.esd.api.request.thinclient;

import java.util.List;

import org.iii.esd.api.MetaId;
import org.iii.esd.api.vo.DeviceReport;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.integrate.TxgDeviceHistory;
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
public class TxgThinClientUploadDataResquest {

	private String resId;

	private TxgElectricData resourceData;

	private List<TxgDeviceHistory> deviceHistories;

	public static TxgThinClientUploadDataResquest from(String metaId, TxgElectricData tv) {
		TxgThinClientUploadDataResquest y = new TxgThinClientUploadDataResquest();
		y.resId = metaId;
		y.setResourceData(tv);
		return y;
	}

}