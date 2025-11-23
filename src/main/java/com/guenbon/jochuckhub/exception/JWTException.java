package com.guenbon.jochuckhub.exception;


import com.guenbon.jochuckhub.exception.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class JWTException extends RuntimeException {
    private final ErrorCode errorCode;

    public JWTException(final ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }


}
