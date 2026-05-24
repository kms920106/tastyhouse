package com.tastyhouse.core.domain.verification.application.dto.command;

public record ConfirmPhoneVerificationCommand(String phoneNumber, String verificationCode) {
}
