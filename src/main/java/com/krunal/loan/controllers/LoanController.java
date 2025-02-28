package com.krunal.loan.controllers;

import com.krunal.loan.aspect.AddUserNames;
import com.krunal.loan.common.DateUtils;
import com.krunal.loan.models.Loan;
import com.krunal.loan.models.LoanContributor;
import com.krunal.loan.models.LoanStatus;
import com.krunal.loan.payload.request.ContributorRequest;
import com.krunal.loan.payload.request.CreateLoanAccountRequest;
import com.krunal.loan.payload.request.EmiScheduleRequest;
import com.krunal.loan.payload.request.UpdateLoanAccountRequest;
import com.krunal.loan.payload.response.MessageResponse;
import com.krunal.loan.repository.LoanRepository;
import com.krunal.loan.security.jwt.JwtUtils;
import com.krunal.loan.service.impl.EmiService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/api/loan")
public class LoanController {

    private static final Logger logger = LoggerFactory.getLogger(LoanController.class);
    private static final String LOAN_NOT_FOUND = "Loan not found with ID: %d";
    private static final String LOAN_STATUS_NOT_PENDING = "Loan status can only be updated when the status is pending for ID: %d";
    private static final String LOAN_STATUS_NOT_PENDING_FOR_DELETE = "Loan can only be deleted when the status is pending for ID: %d";
    private final LoanRepository loanRepository;
    private final JwtUtils jwtUtils;
    private final EmiService emiService;

    public LoanController(LoanRepository loanRepository, JwtUtils jwtUtils, EmiService emiService) {
        this.loanRepository = loanRepository;
        this.jwtUtils = jwtUtils;
        this.emiService = emiService;
    }

    @PostMapping("/create-loan-account")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<MessageResponse> createLoanAccount(@Valid @RequestBody CreateLoanAccountRequest loanAccountRequest) {
        logger.info("Creating loan account for borrower ID: {}", loanAccountRequest.getBorrowerId());

        // Create loan account
        Loan loan = getLoan(loanAccountRequest);
        loan.setLoanAccount("TEMP");
        loan.setStatus(LoanStatus.PENDING.getCode()); // 2 for pending
        loan.setAddUser(jwtUtils.getLoggedInUserDetails().getId());
        loan = loanRepository.save(loan);
        logger.info("Loan account created with temporary ID: {}", loan.getId());

        // Map ContributorRequest to LoanContributor
        List<LoanContributor> loanContributors = new ArrayList<>(getLoanContributors(loan, loanAccountRequest.getContributors()));
        loan.setLoanContributors(loanContributors);
        loan.setLoanAccount(String.format("LN-%07d", loan.getId()));
        loanRepository.save(loan);
        logger.info("Loan account updated with final ID: {}", loan.getLoanAccount());

        return ResponseEntity.ok(new MessageResponse("Loan account created successfully!"));
    }

    @PutMapping("/update-loan-account/{loanId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<MessageResponse> updateLoanAccount(@PathVariable Long loanId, @Valid @RequestBody UpdateLoanAccountRequest loanAccountRequest) {
        logger.info("Updating loan account for loan ID: {}", loanId);

        // Find existing loan account
        Loan existingLoan = loanRepository.findById(loanId).orElse(null);
        if (existingLoan == null) {
            return ResponseEntity.badRequest().body(new MessageResponse(String.format(LOAN_NOT_FOUND, loanId)));
        }
        if (!Objects.equals(existingLoan.getStatus(), LoanStatus.PENDING.getCode())) {
            return ResponseEntity.badRequest().body(new MessageResponse(String.format(LOAN_STATUS_NOT_PENDING, loanId)));
        }
        // Update loan account details
        existingLoan.setLoanDuration(loanAccountRequest.getLoanDuration());
        existingLoan.setLoanAmount(loanAccountRequest.getLoanAmount());
        existingLoan.setInterestRate(loanAccountRequest.getInterestRate());
        existingLoan.setEmiStartDate(DateUtils.getDateFromString(loanAccountRequest.getEmiStartDate(), DateUtils.YMD));
        existingLoan.setNotes(loanAccountRequest.getNotes());
        existingLoan.setPaymentModeId(loanAccountRequest.getPaymentModeId());
        existingLoan.setEmpPerMonth(loanAccountRequest.getEmiPerMonth());

        CreateLoanAccountRequest loanAccountRequest1 = new CreateLoanAccountRequest();
        loanAccountRequest1.setLoanAmount(loanAccountRequest.getLoanAmount());
        loanAccountRequest1.setInterestRate(loanAccountRequest.getInterestRate());
        loanAccountRequest1.setLoanDuration(loanAccountRequest.getLoanDuration());

        existingLoan.setExpectedProfit(calculateExpectedProfit(loanAccountRequest1));
        existingLoan.setUpdatedUser(jwtUtils.getLoggedInUserDetails().getId());

        // Map ContributorRequest to LoanContributor
        List<LoanContributor> newContributors = getLoanContributors(existingLoan, loanAccountRequest.getContributors());

        // Delete extra contributors
        List<Long> newContributorIds = newContributors.stream().map(LoanContributor::getContributorId).toList();
        existingLoan.getLoanContributors().removeIf(contributor -> !newContributorIds.contains(contributor.getContributorId()));

        // Add or update contributors
        for (LoanContributor newContributor : newContributors) {
            Optional<LoanContributor> existingContributorOpt = existingLoan.getLoanContributors().stream().filter(contributor -> contributor.getContributorId().equals(newContributor.getContributorId())).findFirst();
            if (existingContributorOpt.isPresent()) {
                LoanContributor existingContributor = existingContributorOpt.get();
                existingContributor.setContributorAmount(newContributor.getContributorAmount());
                existingContributor.setContributorShares(newContributor.getContributorShares());
                existingContributor.setInterestRate(newContributor.getInterestRate());
                existingContributor.setExpectedProfit(newContributor.getExpectedProfit());
            } else {
                existingLoan.getLoanContributors().add(newContributor);
            }
        }

        // Save updated loan account
        loanRepository.save(existingLoan);
        logger.info("Loan account updated with ID: {}", existingLoan.getLoanAccount());

        return ResponseEntity.ok(new MessageResponse("Loan account updated successfully!"));
    }

    @GetMapping("/{loanId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @AddUserNames
    public ResponseEntity<?> getLoanAccountById(@PathVariable Long loanId) {
        logger.info("Fetching loan account with ID: {}", loanId);

        try {
            // Fetch loan account by ID
            Loan loan = loanRepository.findById(loanId).orElse(null);
            if (loan == null) {
                logger.warn("Loan not found with ID: {}", loanId);
                return ResponseEntity.badRequest().body(new MessageResponse(String.format(LOAN_NOT_FOUND, loanId)));
            }

            // Fetch EMI summary for the loan
            List<Object[]> results = emiService.findEmiSummaryByLoanId(loanId);
            if (results != null && !results.isEmpty()) {
                Object[] result = results.getFirst();

                // Extract received amount and received EMIs
                double receivedAmount = result[0] != null ? ((Number) result[0]).doubleValue() : 0.0;
                int receivedEmis = result[1] != null ? ((Number) result[1]).intValue() : 0;

                // Round received amount to 2 decimal places
                BigDecimal roundedReceivedAmount = BigDecimal.valueOf(receivedAmount).setScale(2, RoundingMode.HALF_UP);

                // Set values in the loan object
                loan.setReceivedAmount(roundedReceivedAmount.doubleValue());
                loan.setReceivedEmis(receivedEmis);

                // Calculate and round expected profit
                BigDecimal expectedProfit = BigDecimal.valueOf(loan.getExpectedProfit()).setScale(2, RoundingMode.HALF_UP);
                loan.setExpectedProfit(expectedProfit.doubleValue());

                // Calculate remaining EMIs
                loan.setRemainingEmis(loan.getLoanDuration() - loan.getReceivedEmis());

                // Calculate and round total amount
                BigDecimal totalAmount = BigDecimal.valueOf(loan.getLoanDuration() * loan.getEmpPerMonth())
                        .setScale(2, RoundingMode.HALF_UP);
                loan.setTotalAmount(totalAmount.doubleValue());

                // Calculate and round outstanding amount
                BigDecimal outstandingAmount = BigDecimal.valueOf(totalAmount.doubleValue() - roundedReceivedAmount.doubleValue())
                        .setScale(2, RoundingMode.HALF_UP);
                loan.setOutstandingAmount(outstandingAmount.doubleValue());
            }
            loan.getLoanContributors().forEach(lb ->{
                lb.setLoanAccount(loan.getLoanAccount());
                lb.setLoanDuration(loan.getLoanDuration());
            });
            loan.getEmis().forEach(emi -> emi.setRemainingAmount(BigDecimal.valueOf(emi.getRemainingAmount()).setScale(2, RoundingMode.HALF_UP).doubleValue()));
            logger.info("Loan account with ID: {} fetched successfully", loanId);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            logger.error("Error fetching loan account with ID: {}", loanId, e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Error fetching loan account"));
        }
    }

    @GetMapping("/loan-list")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getAllLoans() {
        logger.info("Fetching all loans");
        List<Loan> loans = loanRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(loans);
    }

    @DeleteMapping("/{loanId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteLoanAccount(@PathVariable Long loanId) {
        logger.info("Deleting loan account with ID: {}", loanId);

        Optional<Loan> loanOptional = loanRepository.findById(loanId);
        if (loanOptional.isEmpty()) {
            String message = String.format(LOAN_NOT_FOUND, loanId);
            logger.warn(message);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        if (!Objects.equals(loanOptional.get().getStatus(), LoanStatus.PENDING.getCode())) {
            return ResponseEntity.badRequest().body(new MessageResponse(String.format(LOAN_STATUS_NOT_PENDING_FOR_DELETE, loanId)));
        }
        loanRepository.deleteById(loanId);
        logger.info("Loan account with ID {} deleted successfully", loanId);

        return ResponseEntity.ok(new MessageResponse("Loan account deleted successfully!"));
    }

    @PutMapping("/approve/{loanId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<MessageResponse> approveLoanAccount(@PathVariable Long loanId) {
        logger.info("Approving loan account with ID: {}", loanId);

        Loan loan = loanRepository.findById(loanId).orElse(null);
        if (loan == null) {
            return ResponseEntity.badRequest().body(new MessageResponse(String.format(LOAN_NOT_FOUND, loanId)));
        }
        if (!Objects.equals(loan.getStatus(), LoanStatus.PENDING.getCode())) {
            return ResponseEntity.badRequest().body(new MessageResponse(String.format(LOAN_STATUS_NOT_PENDING, loanId)));
        }
        loan.setStatus(LoanStatus.ACTIVE.getCode()); // 1 for approved
        loan.setUpdatedUser(jwtUtils.getLoggedInUserDetails().getId());

        EmiScheduleRequest scheduleRequest = new EmiScheduleRequest();
        scheduleRequest.setLoanId(loan.getId());
        scheduleRequest.setLoanAmount(loan.getLoanAmount());
        scheduleRequest.setInterestRate(loan.getInterestRate());
        scheduleRequest.setNumberOfEmis(loan.getLoanDuration());
        scheduleRequest.setFirstEmiDate(loan.getEmiStartDate());
        emiService.generateEmiSchedule(scheduleRequest);
        loanRepository.updateStatusByLoanId(loan.getId(), loan.getStatus(), loan.getUpdatedUser());

        logger.info("Loan account with ID {} approved successfully", loanId);
        return ResponseEntity.ok(new MessageResponse("Loan account approved successfully!"));
    }

    @PutMapping("/reject/{loanId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<MessageResponse> rejectLoanAccount(@PathVariable Long loanId) {
        logger.info("Rejecting loan account with ID: {}", loanId);

        Loan loan = loanRepository.findById(loanId).orElse(null);
        if (loan == null) {
            return ResponseEntity.badRequest().body(new MessageResponse(String.format(LOAN_NOT_FOUND, loanId)));
        }
        if (!Objects.equals(loan.getStatus(), LoanStatus.PENDING.getCode())) {
            return ResponseEntity.badRequest().body(new MessageResponse(String.format(LOAN_STATUS_NOT_PENDING, loanId)));
        }
        loan.setStatus(LoanStatus.REJECTED.getCode()); // 3 for rejected
        loan.setUpdatedUser(jwtUtils.getLoggedInUserDetails().getId());
        loanRepository.updateStatusByLoanId(loan.getId(), loan.getStatus(), loan.getUpdatedUser());

        logger.info("Loan account with ID {} rejected successfully", loanId);
        return ResponseEntity.ok(new MessageResponse("Loan account rejected successfully!"));
    }

    // Method to map ContributorRequest to LoanContributor
    private List<LoanContributor> getLoanContributors(Loan loan, List<ContributorRequest> contributorRequests) {
        logger.info("Mapping {} contributors for loan ID: {}", contributorRequests.size(), loan.getId());
        return contributorRequests.stream().map(contributorRequest -> {
            LoanContributor loanContributor = new LoanContributor();
            loanContributor.setContributorId(contributorRequest.getContributorId());
            loanContributor.setContributorAmount(contributorRequest.getContributorAmount());
            loanContributor.setContributorShares(contributorRequest.getContributorShares());
            loanContributor.setInterestRate(contributorRequest.getInterestRate());
            loanContributor.setExpectedProfit(contributorRequest.getExpectedProfit());
            loanContributor.setAddUser(jwtUtils.getLoggedInUserDetails().getId());// Set the loan reference
            loanContributor.setLoanId(loan.getId());
            return loanContributor;
        }).toList();
    }

    // Method to map CreateLoanAccountRequest to Loan
    private Loan getLoan(CreateLoanAccountRequest loanAccountRequest) {
        logger.info("Mapping loan account request for borrower ID: {}", loanAccountRequest.getBorrowerId());
        Loan loan = new Loan();
        loan.setBorrowerId(loanAccountRequest.getBorrowerId());
        loan.setLoanDuration(loanAccountRequest.getLoanDuration());
        loan.setLoanAmount(loanAccountRequest.getLoanAmount());
        loan.setInterestRate(loanAccountRequest.getInterestRate());
        loan.setEmiStartDate(DateUtils.getDateFromString(loanAccountRequest.getEmiStartDate(), DateUtils.YMD));
        loan.setNotes(loanAccountRequest.getNotes());
        loan.setPaymentModeId(loanAccountRequest.getPaymentModeId());
        loan.setEmpPerMonth(loanAccountRequest.getEmiPerMonth());
        loan.setExpectedProfit(calculateExpectedProfit(loanAccountRequest));
        return loan;
    }

    private Double calculateExpectedProfit(CreateLoanAccountRequest loanAccountRequest) {
        Double loanAmount = loanAccountRequest.getLoanAmount();
        Double annualInterestRate = loanAccountRequest.getInterestRate();
        Integer loanDurationInMonths = loanAccountRequest.getLoanDuration();

        // Convert annual interest rate to monthly interest rate
        Double monthlyInterestRate = annualInterestRate / 12;

        // Simple interest calculation
        double expectedProfit = loanAmount * monthlyInterestRate * loanDurationInMonths / 100;

        // Round the expected profit to 2 decimal places
        BigDecimal roundedExpectedProfit = BigDecimal.valueOf(expectedProfit).setScale(2, RoundingMode.HALF_UP);

        return roundedExpectedProfit.doubleValue();
    }
}