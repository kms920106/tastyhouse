package com.tastyhouse.application.policy.port.out;

import java.time.LocalDateTime;

public record PolicyDocumentResult(
    Long id,
    String type,
    String version,
    String title,
    String content,
    boolean current,
    boolean mandatory,
    LocalDateTime effectiveDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
