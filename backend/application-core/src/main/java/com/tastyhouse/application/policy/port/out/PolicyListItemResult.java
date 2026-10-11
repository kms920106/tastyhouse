package com.tastyhouse.application.policy.port.out;

import java.time.LocalDateTime;

public record PolicyListItemResult(
    Long id,
    String type,
    String version,
    String title,
    boolean current,
    LocalDateTime effectiveDate,
    LocalDateTime createdAt
) {
}
