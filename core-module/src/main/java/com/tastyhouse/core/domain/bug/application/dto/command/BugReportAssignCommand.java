package com.tastyhouse.core.domain.bug.application.dto.command;

import com.tastyhouse.core.domain.bug.domain.vo.BugReportId;

public record BugReportAssignCommand(
    BugReportId id,
    Long assigneeAdminId
) {

    public static BugReportAssignCommand of(BugReportId id, Long assigneeAdminId) {
        return new BugReportAssignCommand(id, assigneeAdminId);
    }
}
