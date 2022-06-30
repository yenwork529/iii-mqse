package org.iii.esd.auth.vo;

import java.util.Set;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class SignUpRequest {
	
    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private String companyId;

    private String qseId;

    private String txgId;

    private String resId;
    
	private Long siloCompanyId;

	private Long siloFieldId;

	private Long siloAfcId;

	private Long siloDrId;
	
	private Long siloSrId;

    private Set<Long> roleids;

}
