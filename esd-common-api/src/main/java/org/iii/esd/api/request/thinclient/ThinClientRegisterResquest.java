package org.iii.esd.api.request.thinclient;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.iii.esd.mongo.document.FieldProfile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ThinClientRegisterResquest {
	
	@NotBlank(message = "ThinClient IP Address is Required")
	@Pattern(regexp = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$", message = "IP is Invalid")
	private String tcip;

	@NotNull(message = "FieldId is Required")
	@Min(value = 1)
	private Long fieldId;

	public ThinClientRegisterResquest(FieldProfile fieldProfile) {
		this.tcip = fieldProfile.getTcIp();
		this.fieldId = fieldProfile.getId();
	}

}