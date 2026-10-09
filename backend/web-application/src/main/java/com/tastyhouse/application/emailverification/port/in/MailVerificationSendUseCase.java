package com.tastyhouse.application.emailverification.port.in;

public interface MailVerificationSendUseCase {

    void sendVerificationCode(MailVerificationSendCommand command);
}
