package com.krunal.loan.service.impl;

import com.krunal.loan.models.Loan;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;

public class LoanSpecifications {
    public static Specification<Loan> loanDateBetween(LocalDate startDate, LocalDate endDate) {
        return (Root<Loan> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            return builder.between(root.get("addDate"), startDate, endDate);
        };
    }
}
