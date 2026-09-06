package com.tastyhouse.application.bug.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record BugReportAssignCommand(
    Long bugReportId,
    Long assigneeAdminId
) {
    public BugReportAssignCommand {
        if (bugReportId == null || assigneeAdminId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
