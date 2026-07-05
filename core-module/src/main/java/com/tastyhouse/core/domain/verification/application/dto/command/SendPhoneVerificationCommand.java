package com.tastyhouse.core.domain.verification.application.dto.command;

public record SendPhoneVerificationCommand(String phoneNumber) {

    public static SendPhoneVerificationCommand of(String phoneNumber) {
        return new SendPhoneVerificationCommand(phoneNumber);
    }
}
