package com.digitalbanking.api.exception;

import org.springframework.http.HttpStatus;

public class AccountNumberGenerationException extends BusinessException {

    public AccountNumberGenerationException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

}
