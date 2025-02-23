package com.krunal.loan.service.impl;

import com.krunal.loan.aspect.AddUserNames;
import com.krunal.loan.models.Borrower;
import com.krunal.loan.models.Loan;
import com.krunal.loan.repository.BorrowerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class BorrowerService {
    private static final Logger logger = LoggerFactory.getLogger(BorrowerService.class);
    private final BorrowerRepository borrowerRepository;

    public BorrowerService(BorrowerRepository borrowerRepository) {
        this.borrowerRepository = borrowerRepository;
    }

    @AddUserNames
    public List<Borrower> getAllBorrowersList(){
        logger.info("Fetching all borrowers");
        List<Borrower> borrowers = borrowerRepository.findAll(Sort.by(Sort.Direction.DESC, "borrowerId"));
        borrowers.forEach(borrower -> {
            borrower.setNoOfLoan(borrower.getLoans().size());
            borrower.setTotalLoanAmount(borrower.getLoans().stream()
                    .mapToDouble(Loan::getLoanAmount)
                    .sum());
        });
        return borrowers;
    }


}