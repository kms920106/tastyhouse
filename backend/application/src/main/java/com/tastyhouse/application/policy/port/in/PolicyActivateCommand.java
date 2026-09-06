package com.tastyhouse.application.policy.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record PolicyActivateCommand(Long policyDocumentId) {
    public PolicyActivateCommand {
        if (policyDocumentId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static PolicyActivateCommand of(Long policyDocumentId) {
        return new PolicyActivateCommand(policyDocumentId);
    }
}
