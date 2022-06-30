package org.iii.esd.client.afc.simulator.def;

public enum PowerRatioEnum {

	PR_P100(100.0),
	PR_P079(79.0),
	PR_P048(48.0),
	PR_P023(23.0),
	PR_P011(11.0),
	PR_P009(9.0),
	PR_ZERO(0.0),
	PR_N009(-9.0),
	PR_N011(-11.0),
	PR_N023(-23.0),
	PR_N048(-48.0),
	PR_N079(-79.0),
	PR_N100(-100.0),  
	UNDEFINED_POWER(null),
	;
	
	private PowerRatioEnum(Double power) {
		this.powerRatio = power;
	}

	public static PowerRatioEnum of(Double power) {
		for (PowerRatioEnum entity: values()) {
			Double entityPower = entity.getPowerRatio();

			if (entityPower!=null) {
				if (entityPower.equals(power))
					return entity;				
			}
		}
		return UNDEFINED_POWER;		
	}
	
	// Power Ratio: percentage(%)
	private Double powerRatio;

	public Double getPowerRatio() {
		return powerRatio;
	}
}
