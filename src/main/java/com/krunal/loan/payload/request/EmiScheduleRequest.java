package com.krunal.loan.payload.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmiScheduleRequest {
    @Positive(message = "Loan amount must be greater than 0")
    private double loanAmount;

    @Positive(message = "Interest rate must be greater than 0")
    private double interestRate;

    @Positive(message = "Number of EMIs must be greater than 0")
    private int numberOfEmis;

    @FutureOrPresent(message = "EMI date must be in the present or future")
    private Date firstEmiDate;

    @NotNull(message = "Loan ID must not be null")
    private Long loanId;


}