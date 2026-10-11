package com.digitalbanking.api.exception;

public abstract class BusinessException extends RuntimeException {

    protected BusinessException(String message) {
        super(message);
    }

}
