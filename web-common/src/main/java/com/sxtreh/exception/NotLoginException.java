package com.sxtreh.exception;

public class NotLoginException extends RequestException {
    public NotLoginException() {
    }
    public NotLoginException(String msg) {
        super(msg);
    }
}
