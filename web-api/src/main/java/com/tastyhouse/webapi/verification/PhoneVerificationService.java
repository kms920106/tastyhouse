package com.tastyhouse.webapi.verification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.verification.application.PhoneVerificationCommandService;
import com.tastyhouse.core.domain.verification.application.dto.command.ConfirmPhoneVerificationCommand;
import com.tastyhouse.core.domain.verification.application.dto.command.SendPhoneVerificationCommand;
import com.tastyhouse.core.domain.verification.application.dto.result.PhoneVerificationResult;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final PhoneVerificationCommandService phoneVerificationCommandService;

    public void sendVerificationCode(String phoneNumber) {
        phoneVerificationCommandService.sendVerificationCode(SendPhoneVerificationCommand.of(phoneNumber));
    }

    public String confirmVerificationCode(String phoneNumber, String verificationCode) {
        PhoneVerificationResult result = phoneVerificationCommandService.confirmVerificationCode(
            ConfirmPhoneVerificationCommand.of(phoneNumber, verificationCode));
        return result.phoneNumber();
    }
}
