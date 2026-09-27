package com.tastyhouse.application.bug.port.out.write;

import java.time.LocalDateTime;

public record BugReportState(
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
