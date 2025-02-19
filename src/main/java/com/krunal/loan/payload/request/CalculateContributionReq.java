package com.krunal.loan.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalculateContributionReq {
    @NotNull(message = "Loan amount is required")
    @Min(value = 1, message = "Loan amount must be greater than zero")
    private Double loanAmount;

    @NotNull(message = "Contribute amount is required")
    @Min(value = 1, message = "Contribute amount must be greater than zero")
    private Double contributeAmount;

    @NotNull(message = "Interest rate is required")
    @Min(value = 0, message = "Interest rate must be greater than zero")
    private Double interestRate;

    @NotNull(message = "Loan duration is required")
    @Min(value = 1, message = "Loan duration must be greater than zero")
    private Integer loanDuration;
}