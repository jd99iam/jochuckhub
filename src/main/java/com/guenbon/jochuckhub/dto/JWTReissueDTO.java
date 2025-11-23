package com.guenbon.jochuckhub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class JWTReissueDTO {
    private String accessToken;
    private String refreshToken;
    private int refreshTokenExpiresInSeconds;
}
