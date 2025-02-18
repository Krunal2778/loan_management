package com.krunal.loan.repository;

import com.krunal.loan.models.LoanContributor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanContributorRepository extends JpaRepository<LoanContributor, Long> {
    @Query("SELECT lc FROM LoanContributor lc WHERE lc.loanId = :loanId")
    List<LoanContributor> findByLoanId(@Param("loanId") Long loanId);
}
