package com.tastyhouse.application.policy.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.policy.model.PolicyType;

public record PolicyListItemResult(
    Long id,
    PolicyType type,
    String version,
    String title,
    boolean current,
    LocalDateTime effectiveDate,
    LocalDateTime createdAt
) {
}
