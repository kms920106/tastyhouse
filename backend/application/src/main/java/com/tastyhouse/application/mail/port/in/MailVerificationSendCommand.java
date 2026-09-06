package com.tastyhouse.application.mail.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record MailVerificationSendCommand(String email) {
    public MailVerificationSendCommand {
        if (email == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
