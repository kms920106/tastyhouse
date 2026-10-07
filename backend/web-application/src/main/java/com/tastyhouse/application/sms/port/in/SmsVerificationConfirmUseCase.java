package com.tastyhouse.application.sms.port.in;

import com.tastyhouse.application.sms.port.in.SmsVerificationConfirmCommand;

public interface SmsVerificationConfirmUseCase {

    String confirmVerificationCode(SmsVerificationConfirmCommand command);
}
