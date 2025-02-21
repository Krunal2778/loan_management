package com.krunal.loan.models;

import lombok.Getter;

@Getter
public enum UsersStatus {
    ACTIVE(1L, "Active"),
    IN_ACTIVE(2L, "In-Active"),
    SUSPENDED(0L, "Suspended");

    private final Long code;
    private final String displayName;

    UsersStatus(Long code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static UsersStatus fromCode(Long code) {
        if (code == null) return null;
        for (UsersStatus type : UsersStatus.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null; // You can change this to return a default value if needed
    }
}
