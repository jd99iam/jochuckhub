package com.guenbon.jochuckhub.exception.errorcode;

import lombok.Getter;

@Getter
public enum ErrorCode {

    MEMBER_NOT_FOUND("member not found"),

    // ===== JWT =====
    EXPIRED_TOKEN("Expired JWT token"),
    INVALID_SIGNATURE("Invalid JWT signature"),
    MALFORMED_TOKEN("Malformed JWT token"),
    UNSUPPORTED_TOKEN("Unsupported JWT token"),
    TOKEN_NOT_FOUND("Token is missing or empty"),
    TOKEN_MISMATCH("Token does not match server record");

    private String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
