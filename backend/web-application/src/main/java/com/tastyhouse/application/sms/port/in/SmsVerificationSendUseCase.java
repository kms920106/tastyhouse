package com.tastyhouse.application.sms.port.in;

import com.tastyhouse.application.sms.port.in.SmsVerificationSendCommand;

public interface SmsVerificationSendUseCase {

    void sendVerificationCode(SmsVerificationSendCommand command);
}
