package com.tastyhouse.application.mail.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.mail.service.MailVerificationService;
import com.tastyhouse.application.mail.port.in.MailVerificationCommandUseCase;
import com.tastyhouse.application.mail.port.in.MailVerificationConfirmCommand;
import com.tastyhouse.application.mail.port.in.MailVerificationSendCommand;

@Service
@WebApp
public class MailVerificationCommandService implements MailVerificationCommandUseCase {

    private static final Logger log = LoggerFactory.getLogger(MailVerificationCommandService.class);

    private final MailVerificationService mailVerificationService;

    public MailVerificationCommandService(MailVerificationService mailVerificationService) {
        this.mailVerificationService = mailVerificationService;
    }

    @Override
    @Transactional
    public void sendVerificationCode(MailVerificationSendCommand command) {
        mailVerificationService.issueForSignUp(command.email());
    }

    @Override
    @Transactional
    public String confirmVerificationCode(MailVerificationConfirmCommand command) {
        String email = command.email();

        mailVerificationService.confirmForSignUp(email, command.verificationCode());
        log.info("메일 인증 완료. email={}", email);
        return email;
    }
}
