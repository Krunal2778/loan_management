package com.krunal.loan.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BorrowerRequest {
    @Size(max = 50)
    private String name;

    @Size(max = 50)
    private String fatherName;

    @Size(max = 50)
    @Email
    private String email;

    @Size(min = 10, max = 15)
    private String phoneNo;

    @Size(max = 300)
    private String address;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date dob;

    @Size(max = 300)
    private String notes;

    private Set<String> base64Image;
}
