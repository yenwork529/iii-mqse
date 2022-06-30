package org.iii.esd.auth.vo;

import lombok.Data;

@Data
public class JwtResponse {
	
	private String token;
	private String tokenType = "Bearer";
	private long expire;

    public JwtResponse(String token) {
        this.token = token;
    }	

}