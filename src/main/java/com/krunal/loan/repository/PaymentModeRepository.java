package com.krunal.loan.repository;

import com.krunal.loan.models.Emi;
import com.krunal.loan.models.PaymentMode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentModeRepository extends JpaRepository<PaymentMode, Long> {
}
