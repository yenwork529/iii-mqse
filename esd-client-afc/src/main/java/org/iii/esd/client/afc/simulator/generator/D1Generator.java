package org.iii.esd.client.afc.simulator.generator;

import static org.iii.esd.afc.utils.Calculator.getFractionDigits;

import org.iii.esd.client.afc.simulator.def.FPRMappingEnum;

public class D1Generator {	
	
	private static int DURATION_IN_SECONDS = 30;
	
	private static int TEST_CASES_COUNT = FPRMappingEnum.values().length;
	
	public static void main(String[] args) {		
		for (int i=1; i<TEST_CASES_COUNT; i++) {
			for (int j=0; j<DURATION_IN_SECONDS; j++) {
				System.out.println("        - " + getFractionDigits(FPRMappingEnum.of(0).getD1FrequencyEnum().getFrequency().doubleValue(), 2));
			}
			for (int j=0; j<DURATION_IN_SECONDS; j++) {
				System.out.println("        - " + getFractionDigits(FPRMappingEnum.of(i).getD1FrequencyEnum().getFrequency().doubleValue(), 2));
			}	
		}
	}	
}