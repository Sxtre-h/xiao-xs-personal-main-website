package com.sxtreh.exception;

public class FileUploadErrorException extends RequestException {
    public FileUploadErrorException() {
    }

    public FileUploadErrorException(String msg) {
        super(msg);
    }
}
