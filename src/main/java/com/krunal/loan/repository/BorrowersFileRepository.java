package com.krunal.loan.repository;

import com.krunal.loan.models.BorrowersFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowersFileRepository  extends JpaRepository<BorrowersFile, Long> {
    void deleteByBorrowerId(Long id);
}
