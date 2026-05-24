package com.tastyhouse.core.domain.verification.application.dto.command;

public record ConfirmEmailVerificationCommand(String email, String verificationCode) {
}
