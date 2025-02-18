package com.krunal.loan.controllers;

import com.krunal.loan.models.Emi;
import com.krunal.loan.payload.request.EmiScheduleRequest;
import com.krunal.loan.service.impl.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/api/emi")
public class EmiController {

    private final LoanService loanService;

    @Autowired
    public EmiController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/schedule")
    public List<Emi> getEmiSchedule(@RequestBody EmiScheduleRequest emiScheduleRequest) {
        return loanService.generateEmiSchedule(emiScheduleRequest);
    }
}