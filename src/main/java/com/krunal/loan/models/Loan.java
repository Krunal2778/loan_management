package com.krunal.loan.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "loans",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "loan_account")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;

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

    @NotNull
    private Double expectedProfit;

    @Column(nullable = false)
    private Long status;

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
    private Set<LoanContributor> loanContributors = new HashSet<>();

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Emi> emis = new HashSet<>();
}