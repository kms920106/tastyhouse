package com.tastyhouse.core.domain.verification.application.dto.command;

public record ConfirmPhoneVerificationCommand(String phoneNumber, String verificationCode) {

    public static ConfirmPhoneVerificationCommand of(String phoneNumber, String verificationCode) {
        return new ConfirmPhoneVerificationCommand(phoneNumber, verificationCode);
    }
}
