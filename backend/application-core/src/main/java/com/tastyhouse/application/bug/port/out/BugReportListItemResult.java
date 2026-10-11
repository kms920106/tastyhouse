package com.tastyhouse.application.bug.port.out;

import java.time.LocalDateTime;

public record BugReportListItemResult(
    Long id,
    Long memberId,
    String device,
    String title,
    String status,
    String category,
    String priority,
    long imageCount,
    LocalDateTime createdAt
) {
}
