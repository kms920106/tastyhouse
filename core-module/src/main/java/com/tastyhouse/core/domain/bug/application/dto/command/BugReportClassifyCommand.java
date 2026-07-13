package com.tastyhouse.core.domain.bug.application.dto.command;

import com.tastyhouse.core.domain.bug.domain.model.BugReportCategory;
import com.tastyhouse.core.domain.bug.domain.model.BugReportPriority;
import com.tastyhouse.core.domain.bug.domain.vo.BugReportId;

public record BugReportClassifyCommand(
    BugReportId id,
    BugReportCategory category,
    BugReportPriority priority
) {

    public static BugReportClassifyCommand of(BugReportId id, BugReportCategory category, BugReportPriority priority) {
        return new BugReportClassifyCommand(id, category, priority);
    }
}
