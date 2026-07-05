package com.tastyhouse.core.domain.verification.application.dto.command;

public record ConfirmEmailVerificationCommand(String email, String verificationCode) {

    public static ConfirmEmailVerificationCommand of(String email, String verificationCode) {
        return new ConfirmEmailVerificationCommand(email, verificationCode);
    }
}
