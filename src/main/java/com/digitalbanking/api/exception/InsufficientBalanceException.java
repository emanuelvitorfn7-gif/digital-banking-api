package com.digitalbanking.api.exception;

public class InsufficientBalanceException extends BusinessException {

    public InsufficientBalanceException(String message) {
        super(message);
    }

}
