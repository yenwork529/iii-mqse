package org.iii.esd.client.afc.simulator.generator;

import static org.iii.esd.afc.utils.Calculator.getFractionDigits;

import org.iii.esd.client.afc.simulator.def.FPRMappingEnum;

public class D2Generator {

	private static int DURATION_IN_SECONDS = 30;
	
	public static void main(String[] args) {
		double start = FPRMappingEnum.of(11).getD1FrequencyEnum().getFrequency().doubleValue();
		double end = FPRMappingEnum.of(12).getD1FrequencyEnum().getFrequency().doubleValue();		

		//2-1
		double[] descending = interpolate(start, end, DURATION_IN_SECONDS); 		
		for (int i=0; i<descending.length; i++) {
			System.out.println("        - " + getFractionDigits(descending[i], 2));	
		}
		
		//2-2
		double[] ascending = interpolate(end, start, DURATION_IN_SECONDS);
		for (int i=0; i<descending.length; i++) {
			System.out.println("        - " + getFractionDigits(ascending[i], 2));	
		}
	}	

	private static double[] interpolate(double start, double end, int count) {
	    if (count < 2) {
	        throw new IllegalArgumentException("interpolate: illegal count");
	    }
	    double[] array = new double[count + 1];
	    for (int i = 0; i <= count; ++ i) {
	        array[i] = start + i * (end - start) / count;
	    }
	    return array;
	}
}
