package org.iii.esd.api.vo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.SpinReserveBidDetail;
import org.iii.esd.mongo.enums.DeviceType;
import org.iii.esd.mongo.enums.LoadType;
import org.iii.esd.mongo.vo.data.setup.SetupData;
import org.iii.esd.api.vo.SpinReserveRevenueFieldData;
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
public class SpinReserveRevenueSrData {

	private String srName;
	
	/**
	  * 統計資料時間
	 */
	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date time;

	private BigDecimal awardedCounter;

	private BigDecimal noticeCounter;

	private BigDecimal avgRevenueFactor;

	private BigDecimal servingIndex;

	private BigDecimal capacityRevenue;

	private BigDecimal kwhRevenue;

	private BigDecimal revenue;

	private BigDecimal efficiencyRevenue;

	/**
	 * 平均容量報價
	 */
	private BigDecimal avgCapacityPrice;

	/**
	 * 總得標容量
	 */
	private BigDecimal totalAwardedCapacity;

	private List<SpinReserveRevenueFieldData> list;
	
	public SpinReserveRevenueSrData(String srName, Date time, BigDecimal awardedCounter, BigDecimal noticeCounter, BigDecimal avgRevenueFactor,
			BigDecimal capacityRevenue, BigDecimal kwhRevenue, BigDecimal revenue, List<SpinReserveRevenueFieldData> list) {
		this.srName = srName;
		this.time = time;
		this.awardedCounter = awardedCounter;
		this.noticeCounter = noticeCounter;
		this.avgRevenueFactor = avgRevenueFactor;
		this.capacityRevenue = capacityRevenue;
		this.kwhRevenue = kwhRevenue;
		this.revenue = revenue;
		this.list = list;
	}

}
