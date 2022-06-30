package org.iii.esd.api.vo;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class Payload {

	private String companyId;

	private String qseId;

	private String txgId;

	private String resId;
	
	private Long siloCompanyId;

	private Long siloFieldId;

	private Long afcId;

	private Long drId;
	
	private Long srId;

	private Integer qseCode;

	private Integer tgCode;

	private Integer resCode;

	private Integer serviceType;

	private Integer resourceType;
	
	private List<Integer> roles;

}