package com.tastyhouse.application.bug.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record BugReportStatusChangeCommand(
    Long bugReportId,
    String status,
    String answer
) {
    public BugReportStatusChangeCommand {
        if (bugReportId == null || status == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
