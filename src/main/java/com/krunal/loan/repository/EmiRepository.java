package com.krunal.loan.repository;

import com.krunal.loan.models.Emi;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmiRepository extends JpaRepository<Emi, Long> {
}
