package com.krunal.loan.models;

import lombok.Getter;

@Getter
public enum LoanStatus {
    APPROVED(1L, "Approved"),
    PENDING(2L, "Pending"),
    REJECTED(3L, "Rejected"),
    DEFAULTER(4L, "Defaulter");

    private final Long code;
    private final String displayName;

    LoanStatus(Long code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static LoanStatus fromCode(Long code) {
        if (code == null) return null;
        for (LoanStatus type : LoanStatus.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null; // You can change this to return a default value if needed
    }
}
