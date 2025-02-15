package com.krunal.loan.repository;

import com.krunal.loan.models.LoanContributor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanContributorRepository extends JpaRepository<LoanContributor, Long> {
}
