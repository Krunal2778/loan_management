package com.krunal.loan.models;

import lombok.Getter;

@Getter
public enum EmiStatus {
    RECEIVED(1L, "Received"),
    UPCOMING(2L, "Upcoming"),
    PENDING(3L, "Pending"),
    BOUNCE(4L, "Bounce"),
    FORECLOSED(5L, "Foreclosed");

    private final Long code;
    private final String displayName;

    EmiStatus(Long code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static EmiStatus fromCode(Long code) {
        if (code == null) return null;
        for (EmiStatus type : EmiStatus.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null; // You can change this to return a default value if needed
    }
}
