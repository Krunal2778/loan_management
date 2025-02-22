package com.krunal.loan.payload.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ContributorSummary {
    private Double investedAmount;
    private Double netProfitAmount;
    private Double totalAmount;
    private Long noOfLoans;

    public ContributorSummary(Double investedAmount, Double netProfitAmount, Double totalAmount, Long noOfLoans) {
        this.investedAmount = investedAmount;
        this.netProfitAmount = netProfitAmount;
        this.totalAmount = totalAmount;
        this.noOfLoans = noOfLoans;
    }

}