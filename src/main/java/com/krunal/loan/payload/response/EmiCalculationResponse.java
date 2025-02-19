package com.krunal.loan.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmiCalculationResponse {
    private double loanEmi;
    private double totalAmountPayable;
    private double totalInterestPayable;
}
