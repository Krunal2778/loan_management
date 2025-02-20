package com.krunal.loan.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "emis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Emi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emiId;

    @NotNull
    @Column(name = "loan_id", nullable = true)
    private Long loanId;

    @NotNull
    private Integer emiNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate emiDate;

    @NotNull
    private Double emiAmount;

    @NotNull
    private Double emiReceived;

    @NotNull
    private Double remainingAmount;

    private Long paymentMode;

    @Transient
    private String paymentModeName;

    @NotNull
    private Long status;

    @Transient
    private String statusName;

    @Column(length = 300)
    private String notes;

    @Column(nullable = false)
    private Long addUser;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date addDate;

    private Long emiReceivedUser;

    @Size(max = 500)
    private String filePath;

    @Transient
    private String base64Image;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate emiReceivedDate;

    private Long updatedUser;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", insertable = false, updatable = false)
    @JsonIgnore
    private Loan loan;
}