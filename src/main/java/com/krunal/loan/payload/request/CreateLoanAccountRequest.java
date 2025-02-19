package com.krunal.loan.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateLoanAccountRequest {
    @NotNull(message = "Borrower ID is required")
    private Long borrowerId;

    @NotNull(message = "Loan duration is required")
    @Positive(message = "Loan duration must be positive")
    private Integer loanDuration;

    @NotNull(message = "Loan amount is required")
    private Double loanAmount;

    @NotNull(message = "Interest rate is required")
    @Positive(message = "Interest rate must be positive")
    private Double interestRate;

    @NotNull(message = "EMI per month is required")
    @Positive(message = "EMI per month must be positive")
    private Double emiPerMonth;

    @NotNull(message = "EMI start date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private String emiStartDate;

    @Column(length = 300)
    @Size(max = 300, message = "Notes must be at most 300 characters")
    private String notes;

    private Long paymentModeId;

    @NotNull(message = "Contributors are required")
    private List<ContributorRequest> contributors;
}