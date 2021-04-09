package com.pengwz.dynamic.exception;

public class BraveException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BraveException() {
    }

    public BraveException(String message) {
        super(message);
    }

    public BraveException(String message, String detailMessage) {
        super(message + " ==> " + detailMessage);
    }

    public BraveException(String message, Throwable cause) {
        super(message, cause);
    }

    public BraveException(Throwable cause) {
        super(cause);
    }

    public BraveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
