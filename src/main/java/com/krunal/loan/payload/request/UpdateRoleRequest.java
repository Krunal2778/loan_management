package com.krunal.loan.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateRoleRequest {
	@NotBlank
	@Size(min = 3, max = 20)
	private String username;

	@NotBlank
	@Size(max = 50)
	@Email
	private String email;

	@NotBlank
	@Size(min = 3, max = 20)
	private String role;

	@Size(max = 15)
	private String phoneNo;

	private String base64Image;

	public UpdateRoleRequest(@NotBlank @Size(min = 3, max = 20) String username,
			@NotBlank @Size(max = 50) @Email String email, @NotBlank @Size(min = 3, max = 20) String role, @Size(max = 15) String phoneNo) {
		super();
		this.username = username;
		this.email = email;
		this.role = role;
		this.phoneNo = phoneNo;
	}

}
