package com.tastyhouse.application.bug.port.out;

import java.time.LocalDateTime;
import java.util.List;

public record BugReportDetailResult(
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
    List<BugReportImageResult> images,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
