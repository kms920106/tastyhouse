package com.tastyhouse.application.bug.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record BugReportClassifyCommand(
    Long bugReportId,
    String category,
    String priority
) {
    public BugReportClassifyCommand {
        if (bugReportId == null || category == null || priority == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
