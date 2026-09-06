package com.tastyhouse.application.bug.port.out;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPlatform;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;

public record BugReportDetailResult(
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
    List<BugReportImageResult> images,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
