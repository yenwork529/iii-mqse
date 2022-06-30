package org.iii.esd.api.vo;

import java.util.Date;
import java.util.Set;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.mongo.document.SiloCompanyProfile;
import org.iii.esd.mongo.document.DemandResponseProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.mongo.document.SiloUserProfile;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class SiloUser {
	
	@Positive(message = "id is Invalid")
	private Long id;

	@NotNull(message = "name may not be null")
	private String name;
	
	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date updateTimestamp;

	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date createTimestamp;
	
	@NotNull(message = "email may not be null")
	@Email(message = "email Format Invalid")
    private String email;

    private String password;
    
    @NotEmpty(message = "roleIds may not be empty")
	private Set<Long> roleIds;
    
	@Positive(message = "companyId is Invalid")
	private Long companyId;
	
	private String companyName;

	@Positive(message = "afcId is Invalid")
	private Long afcId;
	
	private String afcName;

	@Positive(message = "drId is Invalid")
	private Long drId;
	
	private String drName;

	@Positive(message = "srId is Invalid")
	private Long srId;
	
	private String srName;
	
	@Positive(message = "fieldId is Invalid")
	private Long fieldId;
	
	private String fieldName;

	@Enumerated(EnumType.STRING)
	private EnableStatus enableStatus;
	
	private String[] phones;

	private String lineToken;
	
	private Set<Long> noticeTypes;

	public SiloUser(SiloUserProfile siloUserProfile) {
		super();
		this.id = siloUserProfile.getId();
		this.name = siloUserProfile.getName();
		this.updateTimestamp = siloUserProfile.getUpdateTime();
		this.createTimestamp = siloUserProfile.getCreateTime();
		this.email = siloUserProfile.getEmail();
		//this.password = userProfile.getPassword();
		this.roleIds = siloUserProfile.getRoleIds();
		SiloCompanyProfile siloCompanyProfile = siloUserProfile.getSiloCompanyProfile();
		if(siloCompanyProfile !=null) {
			this.companyId = siloCompanyProfile.getId();
			this.companyName = siloCompanyProfile.getName();
		}
		AutomaticFrequencyControlProfile automaticFrequencyControlProfile = siloUserProfile.getAutomaticFrequencyControlProfile();
		if(automaticFrequencyControlProfile!=null) {
			this.afcId = automaticFrequencyControlProfile.getId();
			this.afcName = automaticFrequencyControlProfile.getName();			
		}
		DemandResponseProfile demandResponseProfile = siloUserProfile.getDemandResponseProfile();
		if(demandResponseProfile!=null) {
			this.drId = demandResponseProfile.getId();
			this.drName = demandResponseProfile.getName();			
		}	
		SpinReserveProfile spinReserveProfile = siloUserProfile.getSpinReserveProfile();
		if(spinReserveProfile!=null) {
			this.srId = spinReserveProfile.getId();
			this.srName = spinReserveProfile.getName();			
		}
		FieldProfile fieldProfile = siloUserProfile.getFieldProfile();
		if(fieldProfile!=null) {
			this.fieldId = fieldProfile.getId();
			this.fieldName = fieldProfile.getName();			
		}
		this.enableStatus = siloUserProfile.getEnableStatus();
		this.phones = siloUserProfile.getPhones();
		this.lineToken = siloUserProfile.getLineToken();
		this.noticeTypes = siloUserProfile.getNoticeTypes();
	}

}