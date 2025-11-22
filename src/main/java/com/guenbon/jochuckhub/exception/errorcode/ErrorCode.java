package com.guenbon.jochuckhub.exception.errorcode;

import lombok.Getter;

@Getter
public enum ErrorCode {

    MEMBER_NOT_FOUND("member not found");

    private String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
