package com.tastyhouse.application.emailverification.port.in;

public interface MailVerificationConfirmUseCase {

    String confirmVerificationCode(MailVerificationConfirmCommand command);
}
