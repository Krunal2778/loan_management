package com.krunal.loan.service.impl;

import com.krunal.loan.models.Emi;
import com.krunal.loan.payload.request.EmiScheduleRequest;
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
public class LoanService {
    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);
    private final EmiRepository emiRepository;

    public LoanService(EmiRepository emiRepository) {
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

    private Double calculateEmiAmount(@NotNull Double loanAmount, @NotNull Double interestRate, @NotNull Integer loanDuration) {
        logger.debug("Calculating EMI amount for loanAmount: {}, interestRate: {}, loanDuration: {}", loanAmount, interestRate, loanDuration);
        double emiAmount;
        Double monthlyInterestRate = interestRate / 100 / 12;
        emiAmount = (loanAmount * monthlyInterestRate * Math.pow(1 + monthlyInterestRate, loanDuration)) / (Math.pow(1 + monthlyInterestRate, loanDuration) - 1);
        logger.debug("Calculated EMI amount: {}", emiAmount);
        return emiAmount;
    }
}