package com.tastyhouse.application.sms.port.in;

public interface SmsVerificationCommandUseCase {

    void sendVerificationCode(SmsVerificationSendCommand command);

    String confirmVerificationCode(SmsVerificationConfirmCommand command);
}
