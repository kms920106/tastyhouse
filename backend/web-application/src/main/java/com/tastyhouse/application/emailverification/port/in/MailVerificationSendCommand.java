package com.tastyhouse.application.emailverification.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record MailVerificationSendCommand(String email) {

    public MailVerificationSendCommand {
        if (email == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
