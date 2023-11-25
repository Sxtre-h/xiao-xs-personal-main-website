package com.sxtreh.exception;

public class ParameterErrorException extends RequestException {
    public ParameterErrorException() {
    }
    public ParameterErrorException(String msg) {
        super(msg);
    }
}
