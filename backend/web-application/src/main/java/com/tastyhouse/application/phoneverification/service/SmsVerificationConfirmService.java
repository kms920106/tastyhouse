package com.tastyhouse.application.phoneverification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.phoneverification.port.in.SmsVerificationConfirmCommand;
import com.tastyhouse.application.phoneverification.port.in.SmsVerificationConfirmUseCase;

@Service
@Transactional
class SmsVerificationConfirmService implements SmsVerificationConfirmUseCase {

    private static final Logger log = LoggerFactory.getLogger(SmsVerificationConfirmService.class);

    private final SmsVerificationService smsVerificationService;

    public SmsVerificationConfirmService(SmsVerificationService smsVerificationService) {
        this.smsVerificationService = smsVerificationService;
    }

    @Override
    public String confirmVerificationCode(SmsVerificationConfirmCommand command) {
        String phoneNumber = command.phoneNumber();

        smsVerificationService.confirm(phoneNumber, command.verificationCode());
        log.info("SMS 인증 완료. phoneNumber={}", phoneNumber);
        return phoneNumber;
    }
}
