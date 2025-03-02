package com.krunal.loan.repository;

import com.krunal.loan.models.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {
    @Modifying
    @Transactional
    @Query("UPDATE Loan l SET l.status = :status, l.updatedUser = :updatedUser WHERE l.id = :loanId")
    void updateStatusByLoanId(@Param("loanId") Long loanId, @Param("status") Long status, @Param("updatedUser") Long updatedUser);

    List<Loan> findByStatusOrderByIdDesc(Long status);

    @Query("SELECT l FROM Loan l WHERE YEAR(l.addDate) = :year AND MONTH(l.addDate) = :month ")
    List<Loan> findByMonth(@Param("year") int year, @Param("month") int month);

}
