package com.tastyhouse.application.policy.port.out.write;

import java.time.LocalDateTime;

public record PolicyDocumentState(
    Long id,
    String type,
    String version,
    String title,
    String content,
    boolean current,
    boolean mandatory,
    LocalDateTime effectiveDate,
    String createdBy,
    String updatedBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
