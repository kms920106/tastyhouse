package com.tastyhouse.application.bug.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record BugReportStatusChangeCommand(
    Long bugReportId,
    String status,
    String answer
) {

    public BugReportStatusChangeCommand {
        if (bugReportId == null || status == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
