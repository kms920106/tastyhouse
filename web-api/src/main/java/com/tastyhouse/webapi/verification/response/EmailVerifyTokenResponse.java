package com.tastyhouse.webapi.verification.response;

public record EmailVerifyTokenResponse(
    String emailVerifyToken
) {
    public static EmailVerifyTokenResponse from(String emailVerifyToken) {
        return new EmailVerifyTokenResponse(emailVerifyToken);
    }
}
