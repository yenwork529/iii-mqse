package org.iii.esd.api.vo;

import java.math.BigDecimal;
import java.util.Date;

import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.serial.BigDecimalSerializer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/*
{
    "timestamp" : 1589169140000,
    "user_code" : "00000000",
    "ai_kw_sum" : 22610,   
    "ai_kwh_sum_kw" : 22610,
    "ai_kwh_sum" : 118712242
}
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class FixSpinReserveData {
	
	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date timestamp;
	
	private String user_code;
	
	/**
	 * 即時功率
	 */
	private BigDecimal ai_kw_sum;
	/**
	 * 即時功率
	 */
	private BigDecimal ai_kwh_sum_kw;
	/**
	 * 累計度數
	 */
	@JsonSerialize(using = BigDecimalSerializer.class)
	private Number ai_kwh_sum;

	public FixSpinReserveData(ElectricData electricData, String user_code) {
		super();
		this.timestamp = electricData.getTime();
		this.user_code = user_code;
		this.ai_kw_sum = electricData.getActivePower();
		this.ai_kwh_sum_kw = electricData.getActivePower();
		this.ai_kwh_sum = electricData.getTotalkWh();
	}

}