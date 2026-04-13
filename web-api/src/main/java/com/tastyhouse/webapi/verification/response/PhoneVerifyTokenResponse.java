package com.tastyhouse.webapi.verification.response;

public record PhoneVerifyTokenResponse(
    String phoneVerifyToken
) {
    public static PhoneVerifyTokenResponse from(String phoneVerifyToken) {
        return new PhoneVerifyTokenResponse(phoneVerifyToken);
    }
}
