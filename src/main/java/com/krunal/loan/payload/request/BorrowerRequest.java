package com.krunal.loan.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BorrowerRequest implements Serializable {
    @NotNull(message = "Name is required")
    @Size(max = 50, message = "Name must be at most 50 characters")
    private String name;

    @Size(max = 50, message = "Father's name must be at most 50 characters")
    private String fatherName;

    @NotNull(message = "Email is required")
    @Size(max = 50, message = "Email must be at most 50 characters")
    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "Phone number is required")
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    private String phoneNo;

    @Size(max = 300, message = "Address must be at most 300 characters")
    private String address;

    @NotNull(message = "Date of birth is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private String dob;

    @Size(max = 300, message = "Notes must be at most 300 characters")
    private String notes;

    private Long status;

    private Set<String> base64Image;
}