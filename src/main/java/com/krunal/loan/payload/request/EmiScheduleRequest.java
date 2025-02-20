package com.krunal.loan.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmiScheduleRequest {
    private double loanAmount;

    private double interestRate;

    private int numberOfEmis;

    private LocalDate firstEmiDate;

    private Long loanId;


}