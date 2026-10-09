package com.tastyhouse.application.emailverification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.emailverification.port.in.MailVerificationConfirmCommand;
import com.tastyhouse.application.emailverification.port.in.MailVerificationConfirmUseCase;

@Service
@Transactional
class MailVerificationConfirmService implements MailVerificationConfirmUseCase {

    private static final Logger log = LoggerFactory.getLogger(MailVerificationConfirmService.class);

    private final MailVerificationService mailVerificationService;

    public MailVerificationConfirmService(MailVerificationService mailVerificationService) {
        this.mailVerificationService = mailVerificationService;
    }

    @Override
    public String confirmVerificationCode(MailVerificationConfirmCommand command) {
        String email = command.email();

        mailVerificationService.confirmForSignUp(email, command.verificationCode());
        log.info("메일 인증 완료. email={}", email);
        return email;
    }
}
