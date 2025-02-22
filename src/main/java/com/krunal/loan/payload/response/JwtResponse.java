package com.krunal.loan.payload.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class JwtResponse {
	private String token;
	private String type = "Bearer";
	private String refreshToken;
	private Long id;
	private String username;
	private List<String> roles;
	private String name;
	private String signatureImage;

	public JwtResponse(String accessToken, String refreshToken, Long id, String username, List<String> roles, String name, String signatureImage) {
		this.token = accessToken;
		this.refreshToken = refreshToken;
		this.id = id;
		this.username = username;
		this.roles = roles;
		this.name = name;
		this.signatureImage = signatureImage;
	}
}


