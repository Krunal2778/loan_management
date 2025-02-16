package com.krunal.loan.repository;

import com.krunal.loan.models.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BorrowerRepository extends JpaRepository<Borrower, Long> {
    Boolean existsByEmail(String email);
    List<Borrower> findByStatus(Long status);
    @Query("SELECT b FROM Borrower b WHERE (b.userAccount LIKE %:keyword% OR b.name LIKE %:keyword% OR b.fatherName LIKE %:keyword% OR b.email LIKE %:keyword% OR b.phoneNo LIKE %:keyword%) AND b.status != 0")
    List<Borrower> searchBorrowers(@Param("keyword") String keyword);

    @Transactional
    @Modifying
    @Query("DELETE FROM BorrowersFile bf WHERE bf.borrower.borrowerId = :borrowerId")
    void deleteBorrowersFilesByBorrowerId(@Param("borrowerId") Long borrowerId);
}
