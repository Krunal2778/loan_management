package com.krunal.loan.exception;

public class UserStatusNotFoundException extends RuntimeException {
    public UserStatusNotFoundException(String message) {
        super(message);
    }
}