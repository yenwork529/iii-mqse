package org.iii.esd.api.vo;

import java.util.Date;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Positive;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.mongo.document.SiloCompanyProfile;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AutomaticFrequencyControl {
	
	@Positive(message = "id is Invalid")
	private Long id;

	private String name;

	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date updateTimestamp;

	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date createTimestamp;

	@Positive(message = "companyId is Invalid")
	private Long companyId;

	private String companyName;

	@Enumerated(EnumType.STRING)
	private EnableStatus enableStatus;

	public AutomaticFrequencyControl(AutomaticFrequencyControlProfile automaticFrequencyControlProfile) {
		this.id = automaticFrequencyControlProfile.getId();
		this.name = automaticFrequencyControlProfile.getName();
		this.updateTimestamp = automaticFrequencyControlProfile.getUpdateTime();
		this.createTimestamp = automaticFrequencyControlProfile.getCreateTime();
		SiloCompanyProfile siloCompanyProfile = automaticFrequencyControlProfile.getSiloCompanyProfile();
		if(siloCompanyProfile !=null) {
			this.companyId = siloCompanyProfile.getId();
			this.companyName = siloCompanyProfile.getName();
		}
		this.enableStatus = automaticFrequencyControlProfile.getEnableStatus();
	}

}