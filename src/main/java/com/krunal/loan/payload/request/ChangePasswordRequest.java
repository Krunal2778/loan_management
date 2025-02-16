package com.krunal.loan.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
	@NotBlank
	private Long id;

	@NotBlank
	private String newPassword;

}
