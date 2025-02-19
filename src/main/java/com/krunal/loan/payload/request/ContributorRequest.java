package com.krunal.loan.payload.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContributorRequest {
    @NotNull(message = "Contributor ID is required")
    private Long contributorId;

    @NotNull(message = "Contributor amount is required")
    @Positive(message = "Contributor amount must be positive")
    private Double contributorAmount;

    @NotNull(message = "Contributor shares are required")
    @Positive(message = "Contributor shares must be positive")
    private Double contributorShares;

    @NotNull(message = "Interest rate is required")
    @Positive(message = "Interest rate must be positive")
    private Double interestRate;

    @NotNull(message = "Expected profit is required")
    @Positive(message = "Expected profit must be positive")
    private Double expectedProfit;
}