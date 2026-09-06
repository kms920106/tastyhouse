package com.tastyhouse.application.sms.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.sms.service.SmsVerificationService;
import com.tastyhouse.application.sms.port.in.SmsVerificationCommandUseCase;
import com.tastyhouse.application.sms.port.in.SmsVerificationConfirmCommand;
import com.tastyhouse.application.sms.port.in.SmsVerificationSendCommand;

@Service
@WebApp
public class SmsVerificationCommandService implements SmsVerificationCommandUseCase {

    private static final Logger log = LoggerFactory.getLogger(SmsVerificationCommandService.class);

    private final SmsVerificationService smsVerificationService;

    public SmsVerificationCommandService(SmsVerificationService smsVerificationService) {
        this.smsVerificationService = smsVerificationService;
    }

    @Override
    @Transactional
    public void sendVerificationCode(SmsVerificationSendCommand command) {
        smsVerificationService.issue(command.phoneNumber());
    }

    @Override
    @Transactional
    public String confirmVerificationCode(SmsVerificationConfirmCommand command) {
        String phoneNumber = command.phoneNumber();

        smsVerificationService.confirm(phoneNumber, command.verificationCode());
        log.info("SMS 인증 완료. phoneNumber={}", phoneNumber);
        return phoneNumber;
    }
}
