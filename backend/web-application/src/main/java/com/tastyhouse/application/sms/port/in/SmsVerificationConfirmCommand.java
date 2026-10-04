package com.tastyhouse.application.sms.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record SmsVerificationConfirmCommand(
    String phoneNumber,
    String verificationCode
) {

    public SmsVerificationConfirmCommand {
        if (phoneNumber == null || verificationCode == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
