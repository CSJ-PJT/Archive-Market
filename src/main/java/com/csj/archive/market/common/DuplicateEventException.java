package com.csj.archive.market.common;

public class DuplicateEventException extends BusinessException {

    public DuplicateEventException(String message) {
        super("DUPLICATE_EVENT", message);
    }
}
