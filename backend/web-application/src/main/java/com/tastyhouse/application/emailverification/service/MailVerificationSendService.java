package com.tastyhouse.application.emailverification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.emailverification.port.in.MailVerificationSendCommand;
import com.tastyhouse.application.emailverification.port.in.MailVerificationSendUseCase;

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
