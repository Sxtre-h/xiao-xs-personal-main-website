package com.sxtreh.exception;

public class AccountAlreadyExistException extends RequestException {
    public AccountAlreadyExistException() {
    }
    public AccountAlreadyExistException(String msg) {
        super(msg);
    }
}