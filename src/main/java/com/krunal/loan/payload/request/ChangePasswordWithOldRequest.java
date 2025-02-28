package com.krunal.loan.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordWithOldRequest {

    @NotBlank(message = "Old password is required")
    @Size(min = 6, max = 40, message = "New password must be between 6 and 40 characters")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 40, message = "New password must be between 6 and 40 characters")
    private String newPassword;
}
