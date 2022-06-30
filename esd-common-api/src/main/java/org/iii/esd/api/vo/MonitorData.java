package org.iii.esd.api.vo;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.utils.DeviceUtils;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MonitorData {
	
	private String fieldId;

	private String fieldName;
	
	private BigDecimal m0;

	private BigDecimal m1;

	private BigDecimal m2;

	private BigDecimal m3;

	private BigDecimal m10;

	@Enumerated(EnumType.STRING)
	private ConnectionStatus tcStatus;
	
	@Enumerated(EnumType.STRING)
	private ConnectionStatus devStatus;

	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date time;

	public MonitorData(ResElectricData electricData, TxgFieldProfile fieldProfile) {
		if (fieldProfile != null) {
			this.fieldId = fieldProfile.getResId();
			this.fieldName = fieldProfile.getName();
			// this.tcStatus = DeviceUtils.checkConnectionStatus(fieldProfile.getTcLastUploadTime());
			this.tcStatus = fieldProfile.getTcStatus();
			this.devStatus = fieldProfile.getDevStatus();
		}
		if (electricData != null) {
			this.m0 = electricData.getM0kW();
			this.m1 = electricData.getM1kW();
			this.m2 = electricData.getM2kW();
			this.m3 = electricData.getM3kW();
			this.m10 = electricData.getM10kW();
			this.time = electricData.getTime();
		}
	}

}