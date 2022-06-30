package org.iii.esd.api.vo;

import java.util.Date;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.enums.TouType;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.mongo.document.SiloCompanyProfile;
import org.iii.esd.mongo.document.DemandResponseProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.PolicyProfile;
import org.iii.esd.mongo.document.SpinReserveProfile;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Field {

	@Positive(message = "id is Invalid")
	private Long id;

	private String name;

	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date updateTimestamp;

	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date createTimestamp;

	@Positive(message = "policyId is Invalid")
	private Long policyId;
	
	private String policyName;

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

	@Min(value = 0, message = "srIndex is Invalid")
	private Integer srIndex;

	@Enumerated(EnumType.STRING)
	private TouType touType;

	private Integer tyod;

	private Integer tyodc;

	private Integer trhd;

	private Integer oyod;

	private Boolean isReserve;

	private Integer targetType;

	private Integer frequency;

	private Integer delay;

	private String stationId;

	@Enumerated(EnumType.STRING)
	private EnableStatus tcEnable;

	private String tcIp;

	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date tcLastUploadTime;

	public Field(FieldProfile fieldProfile) {
		super();
		this.id = fieldProfile.getId();
		this.name = fieldProfile.getName();
		this.updateTimestamp = fieldProfile.getUpdateTime();
		this.createTimestamp = fieldProfile.getCreateTime();
		PolicyProfile policyProfile = fieldProfile.getPolicyProfile();
		if(policyProfile!=null) {
			this.policyId = policyProfile.getId();
			this.policyName = policyProfile.getName();			
		}
		SiloCompanyProfile siloCompanyProfile = fieldProfile.getSiloCompanyProfile();
		if(siloCompanyProfile !=null) {
			this.companyId = siloCompanyProfile.getId();
			this.companyName = siloCompanyProfile.getName();
		}
		AutomaticFrequencyControlProfile automaticFrequencyControlProfile = fieldProfile.getAutomaticFrequencyControlProfile();
		if(automaticFrequencyControlProfile!=null) {
			this.afcId = automaticFrequencyControlProfile.getId();
			this.afcName = automaticFrequencyControlProfile.getName();			
		}
		DemandResponseProfile demandResponseProfile = fieldProfile.getDemandResponseProfile();
		if(demandResponseProfile!=null) {
			this.drId = demandResponseProfile.getId();
			this.drName = demandResponseProfile.getName();			
		}	
		SpinReserveProfile spinReserveProfile = fieldProfile.getSpinReserveProfile();
		if(spinReserveProfile!=null) {
			this.srId = spinReserveProfile.getId();
			this.srName = spinReserveProfile.getName();			
		}
		this.srIndex = fieldProfile.getSrIndex();
		this.touType = fieldProfile.getTouType();
		this.tyod = fieldProfile.getTyod();
		this.tyodc = fieldProfile.getTyodc();
		this.trhd = fieldProfile.getTrhd();
		this.oyod = fieldProfile.getOyod();
		this.isReserve = fieldProfile.getIsReserve();
		this.targetType = fieldProfile.getTargetType();
		this.frequency = fieldProfile.getFrequency();
		this.delay = fieldProfile.getDelay();
		this.stationId = fieldProfile.getStationId();
		this.tcEnable = fieldProfile.getTcEnable();
		this.tcIp = fieldProfile.getTcIp();
		this.tcLastUploadTime = fieldProfile.getTcLastUploadTime();
	}
}