package com.tastyhouse.application.mail.port.in;

import com.tastyhouse.application.mail.port.in.MailVerificationConfirmCommand;

public interface MailVerificationConfirmUseCase {

    String confirmVerificationCode(MailVerificationConfirmCommand command);
}
