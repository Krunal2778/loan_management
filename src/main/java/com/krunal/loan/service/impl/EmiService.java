package com.krunal.loan.service.impl;

import com.krunal.loan.common.DateUtils;
import com.krunal.loan.common.S3BucketUtils;
import com.krunal.loan.exception.FileUploadException;
import com.krunal.loan.exception.LoanCustomException;
import com.krunal.loan.models.Emi;
import com.krunal.loan.payload.request.CalculateContributionReq;
import com.krunal.loan.payload.request.EmiCalculationRequest;
import com.krunal.loan.payload.request.EmiScheduleRequest;
import com.krunal.loan.payload.request.EmiUpdateReq;
import com.krunal.loan.payload.response.ContributionResponse;
import com.krunal.loan.payload.response.EmiCalculationResponse;
import com.krunal.loan.repository.EmiRepository;
import com.krunal.loan.security.jwt.JwtUtils;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class EmiService {
    private static final Logger logger = LoggerFactory.getLogger(EmiService.class);
    private final EmiRepository emiRepository;
    private final JwtUtils jwtUtils;
    private final S3BucketUtils bucketUtils3;

    public EmiService(EmiRepository emiRepository, JwtUtils jwtUtils, S3BucketUtils bucketUtils3) {
        this.emiRepository = emiRepository;
        this.jwtUtils = jwtUtils;
        this.bucketUtils3 = bucketUtils3;
    }

    public void generateEmiSchedule(EmiScheduleRequest emiScheduleRequest) {
        double loanAmount = emiScheduleRequest.getLoanAmount();
        double interestRate = emiScheduleRequest.getInterestRate();
        int loanDuration = emiScheduleRequest.getNumberOfEmis();
        LocalDate emiStartDate = emiScheduleRequest.getFirstEmiDate();
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
        LocalDate emiDate = emiStartDate;

        for (int i = 1; i <= loanDuration; i++) {
            Emi emiSchedule = new Emi();
            emiSchedule.setEmiNo(i);
            emiSchedule.setEmiDate(emiDate);
            emiSchedule.setEmiAmount((double) emiAmount);
            emiSchedule.setEmiReceivedAmount(0.0);
            emiSchedule.setLoanId(loanId);
            emiSchedule.setStatus(2L); // Upcoming
            emiSchedule.setAddUser(jwtUtils.getLoggedInUserDetails().getId());
            remainingAmount -= emiAmount;
            emiSchedule.setRemainingAmount((double) remainingAmount);
            emiSchedule.setEmiDate(emiDate);
            emiDate = emiDate.plusMonths(1);
            emiSchedules.add(emiSchedule);
        }

        // Save the EMI schedules to the repository
        emiRepository.saveAll(emiSchedules);
        logger.info("EMI schedule generated successfully for loanId: {}", loanId);
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

    public List<Emi> getUpcomingEmis(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching upcoming EMIs between {} and {}", startDate, endDate);
        try {
            List<Emi> upcomingEmis = emiRepository.findUpcomingEmis(startDate, endDate);
            if (upcomingEmis.isEmpty()) {
                logger.error("No upcoming EMIs found between {} and {}", startDate, endDate);
                throw new LoanCustomException("No upcoming EMIs found between " + startDate + " and " + endDate);
            }
            logger.info("Found {} upcoming EMIs between {} and {}", upcomingEmis.size(), startDate, endDate);
            return upcomingEmis;
        } catch (Exception e) {
            logger.error("Error occurred while fetching upcoming EMIs: {}", e.getMessage());
            throw new LoanCustomException("Failed to fetch upcoming EMIs");
        }
    }

    public List<Emi> getALLEmiList() {
        logger.info("Fetching all EMIs");
        try {
            List<Emi> allEmis = emiRepository.findAll();
            if (allEmis.isEmpty()) {
                logger.error("No EMIs found");
                throw new LoanCustomException("No EMIs found");
            }
            logger.info("Found {} EMIs", allEmis.size());
            return allEmis;
        } catch (Exception e) {
            logger.error("Error occurred while fetching all EMIs: {}", e.getMessage());
            throw new LoanCustomException("Failed to fetch all EMIs");
        }
    }

    public List<Emi> filterEmisBetweenDates(LocalDate startDate, LocalDate endDate, Long status) {
        logger.info("Filtering EMIs between {} and {} with status {}", startDate, endDate, status);
        try {
            List<Emi> filteredEmis = emiRepository.findAll().stream()
                    .filter(emi -> !emi.getEmiDate().isBefore(startDate) && !emi.getEmiDate().isAfter(endDate) && emi.getStatus().equals(status))
                    .sorted(Comparator.comparing(Emi::getEmiDate)).toList();
            if (filteredEmis.isEmpty()) {
                logger.error("No EMIs found between {} and {} with status {}", startDate, endDate, status);
                throw new LoanCustomException("No EMIs found between " + startDate + " and " + endDate + " with status " + status);
            }
            logger.info("Found {} EMIs between {} and {} with status {}", filteredEmis.size(), startDate, endDate, status);
            return filteredEmis;
        } catch (Exception e) {
            logger.error("Error occurred while filtering EMIs: {}", e.getMessage());
            throw new LoanCustomException("Failed to filter EMIs");
        }
    }

    public List<Emi> findByLoanIdOrderByEmiDateAsc(Long loanId) {
        logger.info("Fetching EMIs for loan ID: {}", loanId);
        List<Emi> emis = emiRepository.findByLoanIdOrderByEmiDateAsc(loanId);
        if (emis.isEmpty()) {
            logger.error("No EMIs found for loan ID: {}", loanId);
            throw new LoanCustomException("No EMIs found for loan ID: " + loanId);
        }
        logger.info("Found {} EMIs for loan ID: {}", emis.size(), loanId);
        return emis;
    }

    public Emi getEmiById(Long id) {
        logger.info("Fetching EMI with ID: {}", id);
        Optional<Emi> emiOptional = emiRepository.findById(id);
        if (emiOptional.isPresent()) {
            logger.info("EMI found with ID: {}", id);
            return emiOptional.get();
        } else {
            logger.error("EMI not found with ID: {}", id);
            throw new LoanCustomException("EMI not found with ID: " + id);
        }
    }

    public void receiveEmiPayment(EmiUpdateReq receivedPaymentReq) {
        logger.info("Processing payment for EMI ID: {}", receivedPaymentReq.getEmiId());
        try {
            Optional<Emi> emiOptional = emiRepository.findById(receivedPaymentReq.getEmiId());
            if (emiOptional.isEmpty()) {
                logger.error("EMI not found with ID: {}", receivedPaymentReq.getEmiId());
                throw new LoanCustomException("EMI not found with ID: " + receivedPaymentReq.getEmiId());
            }

            Emi emi = emiOptional.get();
            emi.setEmiReceivedAmount(receivedPaymentReq.getAmountReceivedAmount());
            emi.setStatus(receivedPaymentReq.getStatusId()); // Paid
            emi.setPaymentMode(receivedPaymentReq.getPaymentType());
            emi.setNotes(receivedPaymentReq.getNotes());
            emi.setEmiReceivedDate(DateUtils.getDateFromString(receivedPaymentReq.getPaymentReceivedDate(), DateUtils.YMD));
            emi.setReceiverName(receivedPaymentReq.getReceiverName());
            emi.setEmiDate(DateUtils.getDateFromString(receivedPaymentReq.getEmiDate(), DateUtils.YMD));
            emi.setUpdatedUser(jwtUtils.getLoggedInUserDetails().getId());

            String filePath = null;
            if (receivedPaymentReq.getBase64Image() != null) {
                filePath = bucketUtils3.uploadImageToS3Bucket(receivedPaymentReq.getBase64Image());
                if (filePath.equals("Error")) {
                    logger.error("Error uploading file to S3 for emi id: {}", receivedPaymentReq.getEmiId());
                    throw new FileUploadException("Error: Uploading file to S3");
                }
                emi.setFilePath(filePath);
            }
            emiRepository.save(emi);
            logger.info("Payment processed successfully for EMI ID: {}", receivedPaymentReq.getEmiId());
        } catch (Exception e) {
            logger.error("Error occurred while processing payment for EMI ID: {}: {}", receivedPaymentReq.getEmiId(), e.getMessage());
            throw new LoanCustomException("Failed to process payment");
        }
    }
}