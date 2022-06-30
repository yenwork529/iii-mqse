package org.iii.esd.client.afc.simulator.def;

import java.math.BigDecimal;

public enum FrequencyEnum {
	
	F00("T00", new BigDecimal(60.00)),
	F01("T01", new BigDecimal(60.01)),
	F02("T02", new BigDecimal(59.99)),
	F03("T03", new BigDecimal(60.02)),
	F04("T04", new BigDecimal(59.98)),
	F05("T05", new BigDecimal(60.10)),
	F06("T06", new BigDecimal(59.90)),
	F07("T07", new BigDecimal(60.25)),
	F08("T08", new BigDecimal(59.75)),
	F09("T09", new BigDecimal(60.40)),
	F10("T10", new BigDecimal(59.60)),
	F11("T11", new BigDecimal(60.50)),
	F12("T12", new BigDecimal(59.50)),
	F13("T13", new BigDecimal(60.60)),
	F14("T14", new BigDecimal(59.40)),
	F15("T15", new BigDecimal(60.00)),
	;
	
	private FrequencyEnum(String code, BigDecimal frequency) {
		this.code = code;
		this.frequency = frequency;
	}
	
	private String code;
	
	private BigDecimal frequency;
	
	public String getCode() {
		return code;
	}
	
	public BigDecimal getFrequency() {
		return frequency;
	}
}
