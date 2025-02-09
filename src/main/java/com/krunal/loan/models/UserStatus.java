package com.krunal.loan.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "user_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatus {
    @Id
    private Long statusId;

    @Column(nullable = false, unique = true)
    private String statusName;
}
