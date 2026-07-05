package com.tastyhouse.webapi.verification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.verification.application.EmailVerificationCommandService;
import com.tastyhouse.core.domain.verification.application.dto.command.ConfirmEmailVerificationCommand;
import com.tastyhouse.core.domain.verification.application.dto.command.SendEmailVerificationCommand;
import com.tastyhouse.core.domain.verification.application.dto.result.EmailVerificationResult;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationCommandService emailVerificationCommandService;

    public void sendVerificationCode(String email) {
        emailVerificationCommandService.sendVerificationCode(SendEmailVerificationCommand.of(email));
    }

    public String confirmVerificationCode(String email, String verificationCode) {
        EmailVerificationResult result = emailVerificationCommandService.confirmVerificationCode(
            ConfirmEmailVerificationCommand.of(email, verificationCode));
        return result.email();
    }
}
