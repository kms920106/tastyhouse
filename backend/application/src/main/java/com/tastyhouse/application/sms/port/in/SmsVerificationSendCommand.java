package com.tastyhouse.application.sms.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record SmsVerificationSendCommand(String phoneNumber) {
    public SmsVerificationSendCommand {
        if (phoneNumber == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
