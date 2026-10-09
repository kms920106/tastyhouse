package com.tastyhouse.application.phoneverification.port.in;

public interface SmsVerificationConfirmUseCase {

    String confirmVerificationCode(SmsVerificationConfirmCommand command);
}
