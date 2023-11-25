package com.sxtreh.exception;

public class PasswordErrorException extends RequestException {
    public PasswordErrorException() {
    }
    public PasswordErrorException(String msg) {
        super(msg);
    }
}
