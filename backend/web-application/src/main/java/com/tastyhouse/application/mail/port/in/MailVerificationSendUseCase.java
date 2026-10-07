package com.tastyhouse.application.mail.port.in;

public interface MailVerificationSendUseCase {

    void sendVerificationCode(MailVerificationSendCommand command);
}
