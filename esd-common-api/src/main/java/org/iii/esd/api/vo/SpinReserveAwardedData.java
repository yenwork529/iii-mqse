package org.iii.esd.api.vo;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.enums.DeviceType;
import org.iii.esd.mongo.enums.LoadType;
import org.iii.esd.mongo.vo.data.setup.SetupData;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class SpinReserveAwardedData {

	private String id;

	@NotNull(message = "name may not be null")
	private String srName;
	
	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date awardedTimestamp;
	
	private BigDecimal awardedCapacity;
	
	private BigDecimal capacityPrice;
	
	private BigDecimal energyPrice;
	
	public SpinReserveAwardedData(String id, String srName, Date awardedTimestamp, BigDecimal awardedCapacity, BigDecimal capacityPrice, BigDecimal energyPrice) {
		this.id = id;
		this.srName = srName;
		this.awardedTimestamp = awardedTimestamp;
		this.awardedCapacity = awardedCapacity;
		this.capacityPrice = capacityPrice;
		this.energyPrice = energyPrice;
	}

}
