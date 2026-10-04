package com.tastyhouse.application.bug.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record BugReportClassifyCommand(
    Long bugReportId,
    String category,
    String priority
) {

    public BugReportClassifyCommand {
        if (bugReportId == null || category == null || priority == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
