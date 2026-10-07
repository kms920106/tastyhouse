package com.tastyhouse.application.mail.port.in;

public interface MailVerificationConfirmUseCase {

    String confirmVerificationCode(MailVerificationConfirmCommand command);
}
