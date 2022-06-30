package org.iii.esd.api.request.taipower;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SpinReserveResquest {
	
	@NotNull(message = "srId is Required")
	@Positive(message = "srId is Invalid")
	private Long srId;
	@NotNull(message = "boValue is Required")
	@PositiveOrZero(message = "boValue is Invalid")
	private Integer boValue;

}