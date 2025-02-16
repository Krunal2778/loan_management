package com.krunal.loan.repository;

import com.krunal.loan.models.BorrowersFile;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowersFileRepository  extends JpaRepository<BorrowersFile, Long> {
    @Transactional
    void deleteByBorrowerId(Long id);

}
