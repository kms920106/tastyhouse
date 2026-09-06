package com.tastyhouse.application.bug.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;

public record BugReportListItemResult(
    Long id,
    Long memberId,
    String device,
    String title,
    BugReportStatus status,
    BugReportCategory category,
    BugReportPriority priority,
    long imageCount,
    LocalDateTime createdAt
) {
}
