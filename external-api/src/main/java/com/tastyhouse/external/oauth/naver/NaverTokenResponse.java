package com.tastyhouse.external.oauth.naver;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverTokenResponse(
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("expires_in") String expiresIn,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("refresh_token_expires_in") String refreshTokenExpiresIn
) {}
