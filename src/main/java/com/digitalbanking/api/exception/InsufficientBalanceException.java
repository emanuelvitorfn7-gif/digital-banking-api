package com.digitalbanking.api.exception;

import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends BusinessException {

    public InsufficientBalanceException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

}
