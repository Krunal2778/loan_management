package com.krunal.loan.controllers;

import com.krunal.loan.payload.request.CalculateContributionReq;
import com.krunal.loan.payload.request.ContributorRequest;
import com.krunal.loan.payload.request.EmiCalculationRequest;
import com.krunal.loan.payload.response.ContributionResponse;
import com.krunal.loan.payload.response.EmiCalculationResponse;
import com.krunal.loan.service.impl.EmiService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


    @PostMapping("/contributor-share")
    public ResponseEntity<ContributionResponse> calculateContributorShare(@Valid @RequestBody CalculateContributionReq request) {
        logger.info("Received contributor share calculation request: {}", request);

        ContributionResponse response = emiService.calculateContribution(request);

        logger.info("Contributor share calculation response: {}", response);
        return ResponseEntity.ok(response);
    }

}