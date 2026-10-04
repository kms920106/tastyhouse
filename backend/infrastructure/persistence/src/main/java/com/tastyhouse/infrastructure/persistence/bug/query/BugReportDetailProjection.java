package com.tastyhouse.infrastructure.persistence.bug.query;

import java.time.LocalDateTime;

public record BugReportDetailProjection(
    Long id,
    Long memberId,
    String device,
    String title,
    String content,
    String status,
    String category,
    String priority,
    Long assigneeAdminId,
    String adminAnswer,
    LocalDateTime resolvedAt,
    String appVersion,
    String platform,
    String osVersion,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
