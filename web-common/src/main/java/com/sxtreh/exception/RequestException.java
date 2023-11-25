package com.sxtreh.exception;

public class RequestException extends RuntimeException {

    public RequestException() {
    }

    public RequestException(String msg) {
        super(msg);
    }

}