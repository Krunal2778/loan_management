package com.krunal.loan.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateLoanAccountRequest {
    @NotNull
    private Long borrowerId;

    @NotNull
    private Integer loanDuration;

    @NotNull
    private Double loanAmount;

    @NotNull
    private Double interestRate;

    @NotNull
    private Double emiPerMonth;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private String emiStartDate;

    @Column(length = 300)
    private String notes;

    private Long paymentModeId;

    @NotNull
    List<ContributorRequest> contributors;
}
