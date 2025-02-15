package com.krunal.loan.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_modes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long paymentModeId;

    @Column(nullable = false, length = 50)
    private String paymentModeStatus;

}
