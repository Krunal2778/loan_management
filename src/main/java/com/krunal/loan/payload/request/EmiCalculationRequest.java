package com.krunal.loan.payload.request;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmiCalculationRequest {
    @Positive(message = "Loan amount must be greater than 0")
    private Double loanAmount;

    @Positive(message = "Interest rate must be greater than 0")
    private Double interestRate;

    @Positive(message = "Number of EMIs must be greater than 0")
    private Integer numberOfEmis;

}