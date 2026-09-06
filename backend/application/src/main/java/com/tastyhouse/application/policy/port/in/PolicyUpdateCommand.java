package com.tastyhouse.application.policy.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record PolicyUpdateCommand(
    Long policyDocumentId,
    String title,
    String content,
    boolean mandatory,
    LocalDateTime effectiveDate,
    String updatedBy
) {
    public PolicyUpdateCommand {
        if (policyDocumentId == null || title == null || content == null || effectiveDate == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
