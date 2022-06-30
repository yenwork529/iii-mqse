package org.iii.esd.api.vo;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.enums.DeviceType;
import org.iii.esd.mongo.enums.LoadType;
import org.iii.esd.mongo.vo.data.setup.SetupData;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SpinReserveRevenueFieldData {

	private String fieldName;

	private BigDecimal awardedCounter;

	private BigDecimal noticeCounter;

	private BigDecimal avgRevenueFactor;

	private BigDecimal capacityRevenue;

	private BigDecimal kWhRevenue;

	private BigDecimal revenue;

	private BigDecimal totalAwardedCapacity;

	private BigDecimal avgCapacityPrice;

	private BigDecimal efficiencyRevenue;

	private BigDecimal servingIndex;
	
	public SpinReserveRevenueFieldData(String fieldName, BigDecimal awardedCounter, BigDecimal noticeCounter, BigDecimal avgRevenueFactor, BigDecimal capacityRevenue, BigDecimal kWhRevenue, BigDecimal revenue) {
		this.fieldName = fieldName;
		this.awardedCounter = awardedCounter;
		this.noticeCounter = noticeCounter;
		this.avgRevenueFactor = avgRevenueFactor;
		this.capacityRevenue = capacityRevenue;
		this.kWhRevenue = kWhRevenue;
		this.revenue = revenue;
	}

}
