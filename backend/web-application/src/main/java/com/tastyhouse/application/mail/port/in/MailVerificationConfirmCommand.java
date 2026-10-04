package com.tastyhouse.application.mail.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record MailVerificationConfirmCommand(
    String email,
    String verificationCode
) {

    public MailVerificationConfirmCommand {
        if (email == null || verificationCode == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
