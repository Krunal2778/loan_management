package com.krunal.loan.models;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum PaymentType {
    CASH(1L, "Cash"),
    BANK_TRANSFER(2L, "UPI"),
    UPI(3L, "Bank Transfer"),
    CREDIT_CARD(4L, "Credit Card"),
    DEBIT_CARD(5L, "Debit Card"),
    CHEQUE(6L, "Cheque"); // Added Cheque

    private final Long code;
    private final String displayName;

    PaymentType(Long code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static PaymentType fromCode(Long code) {
        if (code == null) return null;
        for (PaymentType type : PaymentType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null; // You can change this to return a default value if needed
    }
}
