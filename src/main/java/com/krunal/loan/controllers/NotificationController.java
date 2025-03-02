package com.krunal.loan.controllers;

import com.krunal.loan.models.Loan;
import com.krunal.loan.models.LoanStatus;
import com.krunal.loan.payload.request.EmiScheduleRequest;
import com.krunal.loan.payload.response.MessageResponse;
import com.krunal.loan.repository.LoanRepository;
import com.krunal.loan.security.jwt.JwtUtils;
import com.krunal.loan.service.impl.EmiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private static final String LOAN_NOT_FOUND = "Loan not found with ID: %d";
    private static final String LOAN_STATUS_NOT_PENDING = "Loan status can only be updated when the status is pending for ID: %d";

    private final LoanRepository loanRepository;
    private final JwtUtils jwtUtils;
    private final EmiService emiService;

    @Autowired
    public NotificationController(LoanRepository loanRepository, JwtUtils jwtUtils, EmiService emiService) {
        this.loanRepository = loanRepository;
        this.jwtUtils = jwtUtils;
        this.emiService = emiService;
    }

    @GetMapping("/new-loan-request")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getNewLoanRequest() {
        logger.info("Received request to get new loan requests");
        try {
            List<Loan> loansList = loanRepository.findByStatusOrderByIdDesc(LoanStatus.PENDING.getCode());

            if (loansList.isEmpty()) {
                logger.info("No pending loan requests found.");
                return ResponseEntity.ok(Collections.emptyList());
            }

            logger.info("Found {} pending loan requests", loansList.size());
            return ResponseEntity.ok(loansList);
        } catch (Exception e) {
            logger.error("Error fetching loan requests: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
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


}