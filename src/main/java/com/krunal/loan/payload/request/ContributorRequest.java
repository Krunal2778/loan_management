package com.krunal.loan.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContributorRequest {
    @NotNull
    private Long contributorId;

    @NotNull
    private Double contributorAmount;

    @NotNull
    private Double contributorShares;

    @NotNull
    private Double interestRate;

    @NotNull
    private Double expectedProfit;
}
