package com.tastyhouse.application.sms.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface SmsVerificationCommandUseCase {

    void sendVerificationCode(SmsVerificationSendCommand command);

    String confirmVerificationCode(SmsVerificationConfirmCommand command);
}
