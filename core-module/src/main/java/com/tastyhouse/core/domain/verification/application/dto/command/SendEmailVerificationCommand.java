package com.tastyhouse.core.domain.verification.application.dto.command;

public record SendEmailVerificationCommand(String email) {

    public static SendEmailVerificationCommand of(String email) {
        return new SendEmailVerificationCommand(email);
    }
}
