package org.iii.esd.api.vo;



import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldElectricData {
	
	private Date time;
	
	private BigDecimal kW; 
	
	private BigDecimal activePower;

}