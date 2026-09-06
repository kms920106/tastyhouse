package com.tastyhouse.application.mail.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record MailVerificationConfirmCommand(
    String email,
    String verificationCode
) {
    public MailVerificationConfirmCommand {
        if (email == null || verificationCode == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
