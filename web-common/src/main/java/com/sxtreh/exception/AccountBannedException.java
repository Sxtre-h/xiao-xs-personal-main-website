package com.sxtreh.exception;

public class AccountBannedException extends RequestException {
    public AccountBannedException() {
    }
    public AccountBannedException(String msg) {
        super(msg);
    }
}
