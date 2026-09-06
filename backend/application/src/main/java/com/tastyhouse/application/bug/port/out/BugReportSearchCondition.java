package com.tastyhouse.application.bug.port.out;

import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;

public record BugReportSearchCondition(
    String title,
    String content,
    Long memberId,
    BugReportStatus status,
    BugReportCategory category,
    BugReportPriority priority
) {

    public static BugReportSearchCondition of(
        String title,
        String content,
        Long memberId,
        BugReportStatus status,
        BugReportCategory category,
        BugReportPriority priority
    ) {
        return new BugReportSearchCondition(title, content, memberId, status, category, priority);
    }
}
