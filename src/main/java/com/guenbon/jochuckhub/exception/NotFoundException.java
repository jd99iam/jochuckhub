package com.guenbon.jochuckhub.exception;

import com.guenbon.jochuckhub.exception.errorcode.ErrorCode;

public class NotFoundException extends RuntimeException {

    private ErrorCode errorCode;

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

