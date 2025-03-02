package com.krunal.loan.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashBoardLoanCountsResponse {
    private Integer totalLoanAccounts;
    private Integer activeLoanAccounts;
    private Integer closedLoanAccounts;
    private Integer totalBorrowers;
    private Double totalLoanAmount;
    private Double activeLoanAmount;
    private Double closedLoanAmount;
    private Float totalLoanAccountsIncrease;
    private Float activeLoanAccountsIncrease;
    private Float closedLoanAccountsIncrease;
    private Float totalBorrowersIncrease;

    public Double getActiveLoanAmount() {
        if (activeLoanAmount != null) {
            return BigDecimal.valueOf(activeLoanAmount).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        return null;
    }

    public Double getClosedLoanAmount() {
        if (closedLoanAmount != null) {
            return BigDecimal.valueOf(closedLoanAmount).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        return null;
    }

    public Double getTotalLoanAmount() {
        if (totalLoanAmount != null) {
            return BigDecimal.valueOf(totalLoanAmount).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        return null;
    }

    public Float getActiveLoanAccountsIncrease() {
        if (activeLoanAccountsIncrease != null) {
            return BigDecimal.valueOf(activeLoanAccountsIncrease).setScale(1, RoundingMode.HALF_UP).floatValue();
        }
        return null;
    }

    public Float getClosedLoanAccountsIncrease() {
        if (closedLoanAccountsIncrease != null) {
            return BigDecimal.valueOf(closedLoanAccountsIncrease).setScale(1, RoundingMode.HALF_UP).floatValue();
        }
        return null;
    }

    public Float getTotalLoanAccountsIncrease() {
        if (totalLoanAccountsIncrease != null) {
            return BigDecimal.valueOf(totalLoanAccountsIncrease).setScale(1, RoundingMode.HALF_UP).floatValue();
        }
        return null;
    }

    public Float getTotalBorrowersIncrease() {
        if (totalBorrowersIncrease != null) {
            return BigDecimal.valueOf(totalBorrowersIncrease).setScale(1, RoundingMode.HALF_UP).floatValue();
        }
        return null;
    }
}