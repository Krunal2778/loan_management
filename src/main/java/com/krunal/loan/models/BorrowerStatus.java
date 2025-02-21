package com.krunal.loan.models;

import lombok.Getter;

@Getter
public enum BorrowerStatus {
    ACTIVE(1L, "Active"),
    DEFAULTER(3L, "Defaulter"),
    SUSPENDED(0L, "Suspended");

    private final Long code;
    private final String displayName;

    BorrowerStatus(Long code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static BorrowerStatus fromCode(Long code) {
        if (code == null) return null;
        for (BorrowerStatus type : BorrowerStatus.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null; // You can change this to return a default value if needed
    }
}
