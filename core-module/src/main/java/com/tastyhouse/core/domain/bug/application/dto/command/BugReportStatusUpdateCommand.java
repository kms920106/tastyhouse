package com.tastyhouse.core.domain.bug.application.dto.command;

import com.tastyhouse.core.domain.bug.domain.model.BugReportStatus;
import com.tastyhouse.core.domain.bug.domain.vo.BugReportId;

public record BugReportStatusUpdateCommand(
    BugReportId id,
    BugReportStatus status,
    String answer
) {

    public static BugReportStatusUpdateCommand of(BugReportId id, BugReportStatus status, String answer) {
        return new BugReportStatusUpdateCommand(id, status, answer);
    }
}
