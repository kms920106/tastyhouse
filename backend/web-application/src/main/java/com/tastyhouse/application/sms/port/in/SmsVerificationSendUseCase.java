package com.tastyhouse.application.sms.port.in;

public interface SmsVerificationSendUseCase {

    void sendVerificationCode(SmsVerificationSendCommand command);
}
