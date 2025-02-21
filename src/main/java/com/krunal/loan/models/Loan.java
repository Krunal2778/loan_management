package com.krunal.loan.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "loans",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "loan_account")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long borrowerId;

    @NotNull
    @Column(length = 10)
    private String loanAccount;

    @NotNull
    private Integer loanDuration; // in months

    @NotNull
    private Double interestRate;

    @NotNull
    private Double loanAmount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate emiStartDate;

    @NotNull
    private Double empPerMonth;

    @NotNull
    private Double expectedProfit;

    @Column(nullable = false)
    private Long status;

    @Column(length = 300)
    private String notes;

    @Transient
    private String statusName;

    @NotNull
    private Long paymentModeId;

    @Transient
    private String paymentModeName;

    @Column(nullable = false)
    private Long addUser;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date addDate;

    private Long updatedUser;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<LoanContributor> loanContributors;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Emi> emis ;

    public String getPaymentModeName() {
        PaymentType type = PaymentType.fromCode(this.paymentModeId);
        return (type != null) ? type.getDisplayName() : "Unknown";
    }

    public String getStatusName() {
        LoanStatus loanStatus = LoanStatus.fromCode(this.status);
        return (loanStatus != null) ? loanStatus.getDisplayName() : "Unknown";
    }
}