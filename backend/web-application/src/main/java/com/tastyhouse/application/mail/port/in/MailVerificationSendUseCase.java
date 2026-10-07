package com.tastyhouse.application.mail.port.in;

import com.tastyhouse.application.mail.port.in.MailVerificationSendCommand;

public interface MailVerificationSendUseCase {

    void sendVerificationCode(MailVerificationSendCommand command);
}
