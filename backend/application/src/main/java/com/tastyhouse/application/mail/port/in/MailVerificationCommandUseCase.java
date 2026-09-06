package com.tastyhouse.application.mail.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface MailVerificationCommandUseCase {

    void sendVerificationCode(MailVerificationSendCommand command);

    String confirmVerificationCode(MailVerificationConfirmCommand command);
}
