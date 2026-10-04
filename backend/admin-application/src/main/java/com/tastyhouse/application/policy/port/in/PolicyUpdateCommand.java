package com.tastyhouse.application.policy.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

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
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
