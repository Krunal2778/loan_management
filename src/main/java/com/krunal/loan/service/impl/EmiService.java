package com.krunal.loan.service.impl;

import com.krunal.loan.exception.LoanCustomException;
import com.krunal.loan.models.Emi;
import com.krunal.loan.payload.request.CalculateContributionReq;
import com.krunal.loan.payload.request.EmiCalculationRequest;
import com.krunal.loan.payload.request.EmiScheduleRequest;
import com.krunal.loan.payload.response.ContributionResponse;
import com.krunal.loan.payload.response.EmiCalculationResponse;
import com.krunal.loan.repository.EmiRepository;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class EmiService {
    private static final Logger logger = LoggerFactory.getLogger(EmiService.class);
    private final EmiRepository emiRepository;

    public EmiService(EmiRepository emiRepository) {
        this.emiRepository = emiRepository;
    }

    public List<Emi> generateEmiSchedule(EmiScheduleRequest emiScheduleRequest) {
        double loanAmount = emiScheduleRequest.getLoanAmount();
        double interestRate = emiScheduleRequest.getInterestRate();
        int loanDuration = emiScheduleRequest.getNumberOfEmis();
        Date emiStartDate = emiScheduleRequest.getFirstEmiDate();
        long loanId = emiScheduleRequest.getLoanId();

        logger.info("Generating EMI schedule for loanId: {}", loanId);

        if (loanAmount <= 0) {
            throw new IllegalArgumentException("Loan amount must be greater than zero");
        }
        if (interestRate <= 0) {
            logger.error("Invalid interest rate: {}", interestRate);
            throw new IllegalArgumentException("Interest rate must be greater than zero");
        }
        if (loanDuration <= 0) {
            logger.error("Invalid loan duration: {}", loanDuration);
            throw new IllegalArgumentException("Loan duration must be greater than zero");
        }
        if (emiStartDate == null) {
            logger.error("Invalid EMI start date: {}", emiStartDate);
            throw new IllegalArgumentException("EMI start date must not be null");
        }

        List<Emi> emiSchedules = new ArrayList<>();
        long emiAmount = Math.round(calculateEmiAmount(loanAmount, interestRate, loanDuration));
        long remainingAmount = emiAmount * loanDuration;
        Date emiDate = emiStartDate;

        for (int i = 1; i <= loanDuration; i++) {
            Emi emiSchedule = new Emi();
            emiSchedule.setEmiNo(i);
            emiSchedule.setEmiDate(emiDate);
            emiSchedule.setEmiAmount((double) emiAmount);
            emiSchedule.setEmiReceived(0.0);
            emiSchedule.setLoanId(loanId);
            emiSchedule.setStatus(1L);
            emiSchedule.setAddUser(1L);
            remainingAmount -= emiAmount;
            emiSchedule.setRemainingAmount((double) remainingAmount);
            emiSchedule.setEmiDate(emiDate);
            emiDate = DateUtils.addMonths(emiDate, 1);
            emiSchedules.add(emiSchedule);
        }

        logger.info("EMI schedule generated successfully for loanId: {}", loanId);
        return emiSchedules;
    }

    // Reducing balance formula
    public Double calculateEmiReducingBalAmt(@NotNull Double loanAmount, @NotNull Double interestRate, @NotNull Integer loanDuration) {
        logger.debug("Calculating EMI amount for loanAmount: {}, interestRate: {}, loanDuration: {}", loanAmount, interestRate, loanDuration);
        double emiAmount;
        Double monthlyInterestRate = interestRate / 100 / 12;
        emiAmount = (loanAmount * monthlyInterestRate * Math.pow(1 + monthlyInterestRate, loanDuration)) / (Math.pow(1 + monthlyInterestRate, loanDuration) - 1);
        logger.debug("Calculated EMI amount: {}", emiAmount);
        return emiAmount;
    }

    // Flat rate formula
    private Double calculateEmiAmount(@NotNull Double loanAmount, @NotNull Double interestRate, @NotNull Integer loanDuration) {
        logger.debug("Calculating EMI amount using flat rate formula for loanAmount: {}, interestRate: {}, loanDuration: {}", loanAmount, interestRate, loanDuration);
        double totalInterest = loanAmount * (interestRate / 100) * (loanDuration / 12.0);
        double totalAmount = loanAmount + totalInterest;
        double emiAmount = totalAmount / loanDuration;
        logger.debug("Calculated EMI amount using flat rate formula: {}", emiAmount);
        return emiAmount;
    }

    public EmiCalculationResponse calculateEmi(EmiCalculationRequest emiCalculationRequest) {
        EmiCalculationResponse response = new EmiCalculationResponse();
        try {
            double loanAmount = emiCalculationRequest.getLoanAmount();
            double interestRate = emiCalculationRequest.getInterestRate();
            int numberOfEmis = emiCalculationRequest.getNumberOfEmis();

            logger.info("Calculating EMI for loanAmount: {}, interestRate: {}, numberOfEmis: {}", loanAmount, interestRate, numberOfEmis);

            double emiAmount = calculateEmiAmount(loanAmount, interestRate, numberOfEmis);

            response.setLoanEmi(Math.round(emiAmount));
            response.setTotalAmountPayable(Math.round(emiAmount * numberOfEmis));
            response.setTotalInterestPayable(Math.round((emiAmount * numberOfEmis) - loanAmount));

            logger.info("EMI calculation successful. EMI Amount: {}, Total Amount Payable: {}, Total Interest Payable: {}", response.getLoanEmi(), response.getTotalAmountPayable(), response.getTotalInterestPayable());
        } catch (Exception e) {
            logger.error("Error occurred while calculating EMI: {}", e.getMessage());
            throw new LoanCustomException("Failed to calculate EMI");
        }
        return response;
    }

    public ContributionResponse calculateContribution(CalculateContributionReq contributionReq) {

        double loanAmount = contributionReq.getLoanAmount();
        double contributeAmount = contributionReq.getContributeAmount();
        double interestRate = contributionReq.getInterestRate();
        int loanDuration = contributionReq.getLoanDuration();

        logger.info("Calculating contribution for loanAmount: {}, contributeAmount: {}, interestRate: {}, loanDuration: {}",
                loanAmount, contributeAmount, interestRate, loanDuration);

        if (loanAmount <= 0) {
            logger.error("Invalid loan amount: {}", loanAmount);
            throw new IllegalArgumentException("Loan amount must be greater than zero");
        }
        if (contributeAmount <= 0) {
            logger.error("Invalid contribute amount: {}", contributeAmount);
            throw new IllegalArgumentException("Contribute amount must be greater than zero");
        }
        if (interestRate <= 0) {
            logger.error("Invalid interest rate: {}", interestRate);
            throw new IllegalArgumentException("Interest rate must be greater than zero");
        }
        if (loanDuration <= 0) {
            logger.error("Invalid loan duration: {}", loanDuration);
            throw new IllegalArgumentException("Loan duration must be greater than zero");
        }

        double percentageShare = (contributeAmount / loanAmount) * 100;
        double expectedProfit = contributeAmount * (interestRate / 100) * (loanDuration / 12.0);

        ContributionResponse response = new ContributionResponse();
        response.setPercentageShare(percentageShare);
        response.setExpectedProfit(expectedProfit);

        logger.info("Contribution calculated successfully. Percentage Share: {}, Expected Profit: {}",
                percentageShare, expectedProfit);

        return response;
    }
}