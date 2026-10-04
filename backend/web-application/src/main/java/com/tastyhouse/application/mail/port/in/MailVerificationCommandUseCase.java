package com.tastyhouse.application.mail.port.in;

public interface MailVerificationCommandUseCase {

    void sendVerificationCode(MailVerificationSendCommand command);

    String confirmVerificationCode(MailVerificationConfirmCommand command);
}
