package com.sxtreh.exception;

public class NetDiskSpaceNotEnoughException extends RequestException {
    public NetDiskSpaceNotEnoughException() {
    }

    public NetDiskSpaceNotEnoughException(String msg) {
        super(msg);
    }
}
