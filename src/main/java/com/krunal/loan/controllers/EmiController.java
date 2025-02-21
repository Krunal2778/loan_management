package com.krunal.loan.controllers;

import com.krunal.loan.common.DateUtils;
import com.krunal.loan.exception.LoanCustomException;
import com.krunal.loan.models.Emi;
import com.krunal.loan.payload.request.CalculateContributionReq;
import com.krunal.loan.payload.request.EmiCalculationRequest;
import com.krunal.loan.payload.request.EmiUpdateReq;
import com.krunal.loan.payload.response.ContributionResponse;
import com.krunal.loan.payload.response.EmiCalculationResponse;
import com.krunal.loan.service.impl.EmiService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/emi")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class EmiController {
    private static final Logger logger = LoggerFactory.getLogger(EmiController.class);

    private final EmiService emiService;

    @Autowired
    public EmiController(EmiService emiService) {
        this.emiService = emiService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<EmiCalculationResponse> calculateEmi(@Valid @RequestBody EmiCalculationRequest emiCalculationRequest) {
        logger.info("Received EMI calculation request: {}", emiCalculationRequest);

        EmiCalculationResponse response = emiService.calculateEmi(emiCalculationRequest);

        logger.info("EMI calculation response: {}", response);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/calculate-contributor-share")
    public ResponseEntity<ContributionResponse> calculateContributorShare(@Valid @RequestBody CalculateContributionReq request) {
        logger.info("Received contributor share calculation request: {}", request);

        ContributionResponse response = emiService.calculateContribution(request);

        logger.info("Contributor share calculation response: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<Emi>> getUpcomingEmis(
            @RequestParam("startDate") @DateTimeFormat(pattern = "dd-MM-yyyy") String startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "dd-MM-yyyy") String endDate) {
        logger.info("Received request to get upcoming EMIs between {} and {}", startDate, endDate);
        try {
            LocalDate localDateStart = DateUtils.getDateFromString(startDate, DateUtils.YMD);
            LocalDate localDateEnd = DateUtils.getDateFromString(endDate, DateUtils.YMD);
            Long statusId = 2L; // Upcoming

            List<Emi> upcomingEmis = emiService.filterEmisBetweenDates(localDateStart, localDateEnd, statusId);
            logger.info("Found {} upcoming EMIs", upcomingEmis.size());
            return ResponseEntity.ok(upcomingEmis);
        } catch (LoanCustomException e) {
            logger.error("Error fetching upcoming EMIs between {} and {}: {}", startDate, endDate, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error fetching upcoming EMIs between {} and {}: {}", startDate, endDate, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/emi-list-by-loan-id/{loanId}")
    public ResponseEntity<List<Emi>> getEmisByLoanId(@PathVariable Long loanId) {
        logger.info("Received request to get EMIs for loan ID: {}", loanId);
        try {
            List<Emi> emis = emiService.findByLoanIdOrderByEmiDateAsc(loanId);
            logger.info("Found {} EMIs for loan ID: {}", emis.size(), loanId);
            return ResponseEntity.ok(emis);
        } catch (LoanCustomException e) {
            logger.error("Error fetching EMIs for loan ID: {}: {}", loanId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error fetching EMIs for loan ID: {}: {}", loanId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/emi-list-by-id/{id}")
    public ResponseEntity<Emi> getEmiById(@PathVariable Long id) {
        logger.info("Received request to get EMI by ID: {}", id);
        try {
            Emi emi = emiService.getEmiById(id);
            logger.info("EMI found with ID: {}", id);
            return ResponseEntity.ok(emi);
        } catch (LoanCustomException e) {
            logger.error("Error fetching EMI by ID: {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error fetching EMI by ID: {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/update-emi")
    public ResponseEntity<String> receivePayment(@Valid @RequestBody EmiUpdateReq receivedPaymentReq) {
        logger.info("Received payment request: {}", receivedPaymentReq);
        try {
            emiService.receiveEmiPayment(receivedPaymentReq);
            return ResponseEntity.ok("Payment received successfully");
        } catch (LoanCustomException e) {
            logger.error("Error processing payment for EMI ID: {}: {}", receivedPaymentReq.getEmiId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error processing payment for EMI ID: {}: {}", receivedPaymentReq.getEmiId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process payment");
        }
    }

}