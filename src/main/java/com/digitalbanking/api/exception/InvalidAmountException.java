package com.digitalbanking.api.exception;

import org.springframework.http.HttpStatus;

public class InvalidAmountException extends BusinessException {

    public InvalidAmountException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

}
