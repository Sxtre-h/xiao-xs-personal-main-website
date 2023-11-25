package com.sxtreh.exception;

public class AccountNotFoundException extends RequestException {
    public AccountNotFoundException() {
    }
    public AccountNotFoundException(String msg) {
        super(msg);
    }
}
