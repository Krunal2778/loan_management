package com.krunal.loan.service.impl;

import com.krunal.loan.models.Borrower;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class BorrowerSpecifications {
    public static Specification<Borrower> loanDateBetween(LocalDate startDate, LocalDate endDate) {
        return (Root<Borrower> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            return builder.between(root.get("addDate"), startDate, endDate);
        };
    }
}
