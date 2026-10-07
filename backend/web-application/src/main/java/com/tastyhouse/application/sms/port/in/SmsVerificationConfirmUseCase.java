package com.tastyhouse.application.sms.port.in;

public interface SmsVerificationConfirmUseCase {

    String confirmVerificationCode(SmsVerificationConfirmCommand command);
}
