package com.krunal.loan.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class UpdateRoleRequest {
	@NotBlank
	@Size(min = 6, max = 20)
	private String username;

	@Size(max = 50, message = "Name must be at most 50 characters")
	private String name;

	@Size(max = 50)
	@Email
	private String email;

	private Set<String> role;

	@Size(max = 15)
	private String phoneNo;

	private String base64Image;

	private Long status;

	public UpdateRoleRequest(@NotBlank @Size(min = 6, max = 20) String username,
			 @Size(max = 50) @Email String email, Set<String> role, @Size(max = 15) String phoneNo, String name) {
		super();
		this.username = username;
		this.email = email;
		this.role = role;
		this.phoneNo = phoneNo;
		this.name = name;
	}

}
