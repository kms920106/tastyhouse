package com.tastyhouse.application.sms.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.sms.port.in.SmsVerificationSendCommand;
import com.tastyhouse.application.sms.port.in.SmsVerificationSendUseCase;

@Service
@Transactional
class SmsVerificationSendService implements SmsVerificationSendUseCase {

    private final SmsVerificationService smsVerificationService;

    public SmsVerificationSendService(SmsVerificationService smsVerificationService) {
        this.smsVerificationService = smsVerificationService;
    }

    @Override
    public void sendVerificationCode(SmsVerificationSendCommand command) {
        smsVerificationService.issue(command.phoneNumber());
    }
}
