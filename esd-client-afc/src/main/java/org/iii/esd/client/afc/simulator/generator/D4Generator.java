package org.iii.esd.client.afc.simulator.generator;

import org.iii.esd.client.afc.simulator.def.FPRMappingEnum;

public class D4Generator {

	// run 5 minutes
	private static int DURATION_IN_SECONDS = 300;
	
	public static void main(String[] args) {
		for (int i=0; i<DURATION_IN_SECONDS; i++) {
			System.out.println("        - " + FPRMappingEnum.of(11).getD1FrequencyEnum().getFrequency().doubleValue());	
		}
	}	
}