package org.iii.esd.auth.vo;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignInRequest {
	
	@NotBlank(message = "username is required")
	@Email(message = "mail format is invalid")
	private String username;

	@NotBlank(message = "password is required")
	private String password;

}
