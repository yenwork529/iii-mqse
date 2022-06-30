package org.iii.esd.client.afc.simulator.def;

import static org.iii.esd.client.afc.simulator.def.PowerRatioEnum.PR_N009;
import static org.iii.esd.client.afc.simulator.def.PowerRatioEnum.PR_N011;
import static org.iii.esd.client.afc.simulator.def.PowerRatioEnum.PR_N023;
import static org.iii.esd.client.afc.simulator.def.PowerRatioEnum.PR_N048;
import static org.iii.esd.client.afc.simulator.def.PowerRatioEnum.PR_N079;
import static org.iii.esd.client.afc.simulator.def.PowerRatioEnum.PR_N100;
import static org.iii.esd.client.afc.simulator.def.PowerRatioEnum.PR_P009;
import static org.iii.esd.client.afc.simulator.def.PowerRatioEnum.PR_P011;
import static org.iii.esd.client.afc.simulator.def.PowerRatioEnum.PR_P023;
import static org.iii.esd.client.afc.simulator.def.PowerRatioEnum.PR_P048;
import static org.iii.esd.client.afc.simulator.def.PowerRatioEnum.PR_P079;
import static org.iii.esd.client.afc.simulator.def.PowerRatioEnum.PR_P100;
import static org.iii.esd.client.afc.simulator.def.PowerRatioEnum.UNDEFINED_POWER;

public enum RangeEnum {

	R00("T00", PR_P009, PR_N009),
	R01("T01", PR_N009, PR_P009),
	R02("T02", PR_P009, PR_N009),
	R03("T03", PR_N009, PR_P009),
	R04("T04", PR_P009, PR_N009),
	R05("T05", PR_N011, PR_N023),
	R06("T06", PR_P023, PR_P011),
	R07("T07", PR_N048, PR_N048),
	R08("T08", PR_P048, PR_P048),
	R09("T09", PR_N079, PR_N079),
	R10("T10", PR_P079, PR_P079),
	R11("T11", PR_N100, PR_N100),
	R12("T12", PR_P100, PR_P100),
	R13("T13", PR_N100, PR_N100),
	R14("T14", PR_P100, PR_P100),
	R15("T15", PR_P009, PR_N009),
	UNDEFINED_RANGE("TXX", UNDEFINED_POWER, UNDEFINED_POWER),
	;
	
	private RangeEnum(String code, PowerRatioEnum upperLimit, PowerRatioEnum lowerLimit) {
		this.code = code;
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
	}

	private String code;
	
	// Power Ratio: percentage(%)
	private PowerRatioEnum lowerLimit;

	// Power Ratio: percentage(%)
	private PowerRatioEnum upperLimit;

	public String code() {
		return code;
	}
	
	public PowerRatioEnum getLowerLimit() {
		return lowerLimit;
	}

	public PowerRatioEnum getUpperLimit() {
		return upperLimit;
	}
}
