package com.tastyhouse.application.policy.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record PolicyActivateCommand(Long policyDocumentId) {

    public PolicyActivateCommand {
        if (policyDocumentId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static PolicyActivateCommand of(Long policyDocumentId) {
        return new PolicyActivateCommand(policyDocumentId);
    }
}
