package com.tastyhouse.application.phoneverification.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record SmsVerificationSendCommand(String phoneNumber) {

    public SmsVerificationSendCommand {
        if (phoneNumber == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
