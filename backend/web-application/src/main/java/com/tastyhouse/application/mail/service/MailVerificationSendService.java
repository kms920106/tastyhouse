package com.tastyhouse.application.mail.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.mail.port.in.MailVerificationSendCommand;
import com.tastyhouse.application.mail.port.in.MailVerificationSendUseCase;

@Service
@Transactional
class MailVerificationSendService implements MailVerificationSendUseCase {

    private final MailVerificationService mailVerificationService;

    public MailVerificationSendService(MailVerificationService mailVerificationService) {
        this.mailVerificationService = mailVerificationService;
    }

    @Override
    public void sendVerificationCode(MailVerificationSendCommand command) {
        mailVerificationService.issueForSignUp(command.email());
    }
}
