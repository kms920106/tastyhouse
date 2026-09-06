package com.tastyhouse.application.sms.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record SmsVerificationConfirmCommand(
    String phoneNumber,
    String verificationCode
) {
    public SmsVerificationConfirmCommand {
        if (phoneNumber == null || verificationCode == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
