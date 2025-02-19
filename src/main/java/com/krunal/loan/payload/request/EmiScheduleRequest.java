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
    private double loanAmount;

    private double interestRate;

    private int numberOfEmis;

    private Date firstEmiDate;

    private Long loanId;


}