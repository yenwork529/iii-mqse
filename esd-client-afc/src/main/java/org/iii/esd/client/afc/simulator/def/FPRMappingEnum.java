package org.iii.esd.client.afc.simulator.def;

import static org.iii.esd.client.afc.simulator.def.FrequencyEnum.F00;
import static org.iii.esd.client.afc.simulator.def.FrequencyEnum.F01;
import static org.iii.esd.client.afc.simulator.def.FrequencyEnum.F02;
import static org.iii.esd.client.afc.simulator.def.FrequencyEnum.F03;
import static org.iii.esd.client.afc.simulator.def.FrequencyEnum.F04;
import static org.iii.esd.client.afc.simulator.def.FrequencyEnum.F05;
import static org.iii.esd.client.afc.simulator.def.FrequencyEnum.F06;
import static org.iii.esd.client.afc.simulator.def.FrequencyEnum.F07;
import static org.iii.esd.client.afc.simulator.def.FrequencyEnum.F08;
import static org.iii.esd.client.afc.simulator.def.FrequencyEnum.F09;
import static org.iii.esd.client.afc.simulator.def.FrequencyEnum.F10;
import static org.iii.esd.client.afc.simulator.def.FrequencyEnum.F11;
import static org.iii.esd.client.afc.simulator.def.FrequencyEnum.F12;
import static org.iii.esd.client.afc.simulator.def.FrequencyEnum.F13;
import static org.iii.esd.client.afc.simulator.def.FrequencyEnum.F14;
import static org.iii.esd.client.afc.simulator.def.FrequencyEnum.F15;

import static org.iii.esd.client.afc.simulator.def.RangeEnum.R00;
import static org.iii.esd.client.afc.simulator.def.RangeEnum.R01;
import static org.iii.esd.client.afc.simulator.def.RangeEnum.R02;
import static org.iii.esd.client.afc.simulator.def.RangeEnum.R03;
import static org.iii.esd.client.afc.simulator.def.RangeEnum.R04;
import static org.iii.esd.client.afc.simulator.def.RangeEnum.R05;
import static org.iii.esd.client.afc.simulator.def.RangeEnum.R06;
import static org.iii.esd.client.afc.simulator.def.RangeEnum.R07;
import static org.iii.esd.client.afc.simulator.def.RangeEnum.R08;
import static org.iii.esd.client.afc.simulator.def.RangeEnum.R09;
import static org.iii.esd.client.afc.simulator.def.RangeEnum.R10;
import static org.iii.esd.client.afc.simulator.def.RangeEnum.R11;
import static org.iii.esd.client.afc.simulator.def.RangeEnum.R12;
import static org.iii.esd.client.afc.simulator.def.RangeEnum.R13;
import static org.iii.esd.client.afc.simulator.def.RangeEnum.R14;
import static org.iii.esd.client.afc.simulator.def.RangeEnum.R15;

public enum FPRMappingEnum {

	FPRM_T00("T00", 0, F00, R00),
	FPRM_T01("T01", 1, F01, R01),
	FPRM_T02("T02", 2, F02, R02),
	FPRM_T03("T03", 3, F03, R03),
	FPRM_T04("T04", 4, F04, R04),
	FPRM_T05("T05", 5, F05, R05),
	FPRM_T06("T06", 6, F06, R06),
	FPRM_T07("T07", 7, F07, R07),
	FPRM_T08("T08", 8, F08, R08),
	FPRM_T09("T09", 9, F09, R09),
	FPRM_T10("T10", 10, F10, R10),
	FPRM_T11("T11", 11, F11, R11),
	FPRM_T12("T12", 12, F12, R12),
	FPRM_T13("T13", 13, F13, R13),
	FPRM_T14("T14", 14, F14, R14),
	FPRM_T15("T15", 15, F15, R15),
	;
	
	private FPRMappingEnum(String name, int index, FrequencyEnum frequency, RangeEnum range) {
		this.name = name;
		this.index = index;
		this.frequency = frequency;
		this.range = range;
	}
	
	public static FPRMappingEnum of(int index) {
		for (FPRMappingEnum entity: values()) {
			if (entity.getIndex()==index)
				return entity;
		}
		return FPRM_T00;
	}
	
	private String name;
	
	private int index;
	
	private FrequencyEnum frequency; 
	
	private RangeEnum range;

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}
	
	public FrequencyEnum getD1FrequencyEnum() {
		return frequency;
	}
	
	public RangeEnum getD1RangeEnum() {
		return range;
	}
}
