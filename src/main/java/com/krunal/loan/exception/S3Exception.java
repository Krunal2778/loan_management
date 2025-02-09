package com.krunal.loan.exception;

public class S3Exception extends CustomException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -7060321514770887667L;

    /**
     * @param message
     * @param args
     */
    public S3Exception(String message, Object... args) {
        super(String.format(message, args));
    }
}
