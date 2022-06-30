package org.iii.esd.api.request.taipower;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DnpRemoteSensingRequset {

	/**
	 * 參與者端應以每分鐘1筆之頻率，提供台電端該參與者負載之需量總和資訊。此功率為瞬時值，單位為kW。
	 */
	private BigDecimal ai_kw_sum;
	/**
	 * 參與者端應以每分鐘1筆之頻率，提供台電端該參與者所轄各用戶計量表之負載需量資訊。此功率為瞬時值，單位為kW。
	 */
	private List<BigDecimal> ai_kw_num;
	/**
	 * 參與者端應以每分鐘1筆之頻率，提供台電端該參與者負載千瓦時總和資訊。單位為kWh。
	 */
	private BigDecimal ai_kwh_sum;
	/**
	 * 參與者端應以每分鐘1筆之頻率，提供台電端該參與者所轄各用戶計量表之負載千瓦時資訊。單位為kWh。
	 */
	private List<BigDecimal> ai_kwh_num;
	/*
	 * 參與者之資源中，若包含儲能設備，需由參與者以每分鐘1筆之頻率，提供台電端該參與者所轄各個儲能設備之SOC資訊。單位為%。
	 */
	private List<BigDecimal> ai_ssoc_num;
	/**
	 * 參與者之資源中，若包含儲能設備，需由參與者以每分鐘1筆之頻率，提供台電端該參與者所轄各個儲能設備儲存之千瓦時能量。單位為kWh。
	 */
	private List<BigDecimal> ai_skwh_num;

}