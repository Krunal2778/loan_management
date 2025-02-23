package com.krunal.loan.repository;

import com.krunal.loan.models.Emi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmiRepository extends JpaRepository<Emi, Long> {
    @Query("SELECT e FROM Emi e WHERE e.loanId = :loanId")
    List<Emi> findByLoanId(@Param("loanId") Long loanId);

    @Query("SELECT e FROM Emi e WHERE e.status = 2 AND e.emiDate BETWEEN :startDate AND :endDate ORDER BY e.emiDate ASC")
    List<Emi> findUpcomingEmis(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<Emi> findByLoanIdOrderByEmiDateAsc(Long loanId);

    @Query(value = "SELECT SUM(emi_received_amount) as receivedAmount, COUNT(*) as receivedEmis FROM emis WHERE status = 1 AND loan_id = :loanId", nativeQuery = true)
    List<Object[]> findEmiSummaryByLoanId(@Param("loanId") Long loanId);
}
