package com.tastyhouse.infrastructure.bug.query;

import com.tastyhouse.application.bug.port.out.BugReportDetailResult;
import java.time.LocalDateTime;

import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPlatform;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;

public record BugReportDetailProjection(
    Long id,
    Long memberId,
    String device,
    String title,
    String content,
    BugReportStatus status,
    BugReportCategory category,
    BugReportPriority priority,
    Long assigneeAdminId,
    String adminAnswer,
    LocalDateTime resolvedAt,
    String appVersion,
    BugReportPlatform platform,
    String osVersion,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
