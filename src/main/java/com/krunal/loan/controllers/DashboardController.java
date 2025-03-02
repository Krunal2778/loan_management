package com.krunal.loan.controllers;

import com.krunal.loan.models.Emi;
import com.krunal.loan.models.EmiStatus;
import com.krunal.loan.models.Loan;
import com.krunal.loan.payload.response.DashBoardLoanCountsResponse;
import com.krunal.loan.payload.response.EmiListByDateResponse;
import com.krunal.loan.repository.BorrowerRepository;
import com.krunal.loan.repository.LoanRepository;
import com.krunal.loan.service.impl.DashBoardService;
import com.krunal.loan.service.impl.EmiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    private final EmiService emiService;
    private final LoanRepository loanRepository;
    private final BorrowerRepository borrowerRepository;
    private  final DashBoardService dashBoardService;

    public DashboardController(EmiService emiService, LoanRepository loanRepository, BorrowerRepository borrowerRepository, DashBoardService dashBoardService) {
        this.emiService = emiService;
        this.loanRepository = loanRepository;
        this.borrowerRepository = borrowerRepository;
        this.dashBoardService = dashBoardService;
    }

    @GetMapping("/emi-list")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<EmiListByDateResponse> getDashboardEmi() {
        logger.info("Received request to get EMI list for dashboard");
        try {
            EmiListByDateResponse response = new EmiListByDateResponse();

            List<Emi> allEmis = emiService.getALLEmiList();

            List<Emi> upcomingEmis = allEmis.stream()
                    .filter(emi -> emi.getStatus().equals(EmiStatus.PENDING.getCode()))
                    .limit(12)
                    .sorted(Comparator.comparing(Emi::getEmiDate))
                    .toList();
            logger.info("Found {} upcoming EMIs", upcomingEmis.size());
            response.setUpcomingEmis(upcomingEmis);

            List<Emi> receivedEmis = allEmis.stream()
                    .filter(emi -> emi.getStatus().equals(EmiStatus.APPROVED.getCode()))
                    .limit(12)
                    .sorted(Comparator.comparing(Emi::getUpdatedDate).reversed())
                    .toList();
            logger.info("Found {} received EMIs", receivedEmis.size());
            response.setReceivedEmis(receivedEmis);

            List<Emi> bouncedEmis = allEmis.stream()
                    .filter(emi -> emi.getStatus().equals(EmiStatus.FAILED.getCode()))
                    .limit(12)
                    .sorted(Comparator.comparing(Emi::getUpdatedDate).reversed())
                    .toList();
            logger.info("Found {} bounced EMIs", bouncedEmis.size());
            response.setBouncedEmis(bouncedEmis);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching EMI list for dashboard: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/recent-account")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getRecentAccount() {
        logger.info("Received request to fetch recent loan accounts");
        try {
            logger.info("Fetching up to 12 recent loans");
            Pageable pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "id"));
            List<Loan> loans = loanRepository.findAll(pageable).getContent();
            loans.forEach(loan -> {
                try {
                    String borrowerName = borrowerRepository.findById(loan.getBorrowerId())
                            .orElseThrow(() -> new RuntimeException("Borrower not found"))
                            .getName();
                    loan.setBorrowerName(borrowerName);
                } catch (Exception e) {
                    logger.error("Error fetching borrower name for loan id {}: {}", loan.getId(), e.getMessage(), e);
                }
            });
            logger.info("Successfully fetched {} recent loans", loans.size());
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            logger.error("Error fetching recent loan accounts: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }


    @GetMapping("/counts")
    public ResponseEntity<DashBoardLoanCountsResponse> getLoanCounts(
            @RequestParam("startDate") @DateTimeFormat(pattern = "dd-MM-yyyy") String startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "dd-MM-yyyy") String endDate) {
        DashBoardLoanCountsResponse response;
        response = dashBoardService.getDashBoardLoanCountsResponse(startDate, endDate);
        return ResponseEntity.ok(response);
    }

}
