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

    @Query("SELECT lc FROM LoanContributor lc JOIN Loan l ON lc.loanId = l.id WHERE lc.contributorId = :contributorId")
    List<LoanContributor> findByContributorId(@Param("contributorId") Long contributorId);


    @Query(value = "SELECT SUM(contributor_amount) as investedAmount, SUM(expected_profit) as netProfitAmount, COUNT(*) as noOfLoans FROM loan_contributor WHERE contributor_id = :contributorId", nativeQuery = true)
    List<Object[]> findContributorSummaryByContributorId(@Param("contributorId") Long contributorId);

}
