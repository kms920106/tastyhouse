package com.tastyhouse.application.policy.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.policy.model.PolicyType;

public record PolicyDocumentResult(
    Long id,
    PolicyType type,
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
