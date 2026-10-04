package com.tastyhouse.application.bug.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record BugReportAssignCommand(
    Long bugReportId,
    Long assigneeAdminId
) {

    public BugReportAssignCommand {
        if (bugReportId == null || assigneeAdminId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
