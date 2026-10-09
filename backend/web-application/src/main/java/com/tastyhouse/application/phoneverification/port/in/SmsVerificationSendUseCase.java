package com.tastyhouse.application.phoneverification.port.in;

public interface SmsVerificationSendUseCase {

    void sendVerificationCode(SmsVerificationSendCommand command);
}
