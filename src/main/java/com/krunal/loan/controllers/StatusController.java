package com.krunal.loan.controllers;

import com.krunal.loan.models.PaymentMode;
import com.krunal.loan.models.UserStatus;
import com.krunal.loan.repository.PaymentModeRepository;
import com.krunal.loan.repository.UserStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/status-list")
public class StatusController {
    private static final Logger logger = LoggerFactory.getLogger(StatusController.class);

    private final PaymentModeRepository paymentModeRepository;
    private final UserStatusRepository userStatusRepository;
    @Autowired
    public StatusController(PaymentModeRepository paymentModeRepository, UserStatusRepository userStatusRepository) {
        this.paymentModeRepository = paymentModeRepository;
        this.userStatusRepository = userStatusRepository;
    }

    @GetMapping("/payment-mode")
    public ResponseEntity<List<PaymentMode>> getPaymentModes() {
        logger.info("Received request to get payment modes");
        try {
            List<PaymentMode> paymentModes = paymentModeRepository.findAll();
            logger.info("Found {} payment modes", paymentModes.size());
            return ResponseEntity.ok(paymentModes);
        } catch (Exception e) {
            logger.error("Error fetching payment modes: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/emi")
    public ResponseEntity<List<UserStatus>> getEmiStatusList() {
        logger.info("Received request to get EMI status list");
        try {
            List<UserStatus> emiStatusList = userStatusRepository.findAll().stream()
                    .filter(status -> "EMI".equals(status.getStatusType()))
                    .toList();
            logger.info("Found {} EMI statuses", emiStatusList.size());
            return ResponseEntity.ok(emiStatusList);
        } catch (Exception e) {
            logger.error("Error fetching EMI statuses: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/partner")
    public ResponseEntity<List<UserStatus>> getPartnersList() {
        logger.info("Received request to get partner status list");
        try {
            List<UserStatus> partnersList = userStatusRepository.findAll().stream()
                    .filter(status -> "USER".equals(status.getStatusType()))
                    .toList();
            logger.info("Found {} partner statuses", partnersList.size());
            return ResponseEntity.ok(partnersList);
        } catch (Exception e) {
            logger.error("Error fetching partner statuses: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/borrower")
    public ResponseEntity<List<UserStatus>> getBorrowersStatusList() {
        logger.info("Received request to get borrower status list");
        try {
            List<UserStatus> borrowersList = userStatusRepository.findAll().stream()
                    .filter(status -> "BORROWER".equals(status.getStatusType()))
                    .toList();
            logger.info("Found {} borrower statuses", borrowersList.size());
            return ResponseEntity.ok(borrowersList);
        } catch (Exception e) {
            logger.error("Error fetching borrower statuses: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/loan")
    public ResponseEntity<List<UserStatus>> getLoanStatusList() {
        logger.info("Received request to get loan status list");
        try {
            List<UserStatus> loanStatusList = userStatusRepository.findAll().stream()
                    .filter(status -> "LOAN".equals(status.getStatusType()))
                    .toList();
            logger.info("Found {} loan statuses", loanStatusList.size());
            return ResponseEntity.ok(loanStatusList);
        } catch (Exception e) {
            logger.error("Error fetching loan statuses: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
}