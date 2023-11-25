package com.sxtreh.exception;

public class DataNotExistException extends RequestException {
    public DataNotExistException() {
    }

    public DataNotExistException(String msg) {
        super(msg);
    }
}
