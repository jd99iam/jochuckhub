package com.guenbon.jochuckhub.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JWTReissueDTO {
    private String accessToken;
    private String refreshToken;
}
