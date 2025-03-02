package com.krunal.loan.controllers;

import com.krunal.loan.common.DateUtils;
import com.krunal.loan.common.S3BucketUtils;
import com.krunal.loan.exception.LoanCustomException;
import com.krunal.loan.models.Emi;
import com.krunal.loan.models.EmiStatus;
import com.krunal.loan.payload.request.CalculateContributionReq;
import com.krunal.loan.payload.request.EmiCalculationRequest;
import com.krunal.loan.payload.request.EmiUpdateReq;
import com.krunal.loan.payload.response.ContributionResponse;
import com.krunal.loan.payload.response.EmiCalculationResponse;
import com.krunal.loan.payload.response.EmiListByDateResponse;
import com.krunal.loan.service.impl.EmiService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/emi")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class EmiController {
    private static final Logger logger = LoggerFactory.getLogger(EmiController.class);

    private final EmiService emiService;
    private final S3BucketUtils bucketUtils;

    @Autowired
    public EmiController(EmiService emiService, S3BucketUtils bucketUtils) {
        this.emiService = emiService;
        this.bucketUtils = bucketUtils;
    }

    @PostMapping("/calculate")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<EmiCalculationResponse> calculateEmi(@Valid @RequestBody EmiCalculationRequest emiCalculationRequest) {
        logger.info("Received EMI calculation request: {}", emiCalculationRequest);

        EmiCalculationResponse response = emiService.calculateEmi(emiCalculationRequest);

        logger.info("EMI calculation response: {}", response);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/calculate-contributor-share")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ContributionResponse> calculateContributorShare(@Valid @RequestBody CalculateContributionReq request) {
        logger.info("Received contributor share calculation request: {}", request);

        ContributionResponse response = emiService.calculateContribution(request);

        logger.info("Contributor share calculation response: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/emi-list-by-date")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getEmiListByDateResponse(
            @RequestParam("startDate") @DateTimeFormat(pattern = "dd-MM-yyyy") String startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "dd-MM-yyyy") String endDate) {
        logger.info("Received request to get EMIs between {} and {}", startDate, endDate);
        try {
            LocalDate localDateStart = DateUtils.getDateFromString(startDate, DateUtils.YMD);
            LocalDate localDateEnd = DateUtils.getDateFromString(endDate, DateUtils.YMD);
            EmiListByDateResponse response =new EmiListByDateResponse();
            List<Emi> upcomingEmis = emiService.filterEmisBetweenDates(localDateStart, localDateEnd, EmiStatus.PENDING.getCode());
            upcomingEmis.sort(Comparator.comparing(Emi::getEmiDate));
            logger.info("Found upcoming {} EMIs", upcomingEmis.size());
            response.setUpcomingEmis(upcomingEmis);
            List<Emi> receivedEmis = emiService.filterEmisBetweenDates(localDateStart, localDateEnd, EmiStatus.APPROVED.getCode());
            receivedEmis.sort(Comparator.comparing(Emi::getEmiDate).reversed());
            logger.info("Found received {} EMIs", receivedEmis.size());
            response.setReceivedEmis(receivedEmis);
            List<Emi> bouncedEmis = emiService.filterEmisBetweenDates(localDateStart, localDateEnd, EmiStatus.FAILED.getCode());
            bouncedEmis.sort(Comparator.comparing(Emi::getEmiDate).reversed());
            logger.info("Found bounced {} EMIs", bouncedEmis.size());
            response.setBouncedEmis(bouncedEmis);
            return ResponseEntity.ok(response);
        } catch (DateTimeParseException e) {
            logger.error("Invalid date format: expected format is yyyy-MM-dd", e);
            return ResponseEntity.badRequest().body("Invalid date format. Expected format: yyyy-MM-dd");
        } catch (LoanCustomException e) {
            logger.error("Error fetching EMIs between {} and {}: {}", startDate, endDate, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error fetching  EMIs between {} and {}: {}", startDate, endDate, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/emi-list-by-loan-id/{loanId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Emi> getEmiById(@PathVariable Long id) {
        logger.info("Received request to get EMI by ID: {}", id);
        try {
            Emi emi = emiService.getEmiById(id);
            if (emi.getFilePath() != null) {
                emi.setBase64Image(this.bucketUtils.getFileFromS3(emi.getFilePath()));
            }
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
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
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