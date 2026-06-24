package com.tastyhouse.core.domain.policy.application.dto.result;

import com.querydsl.core.annotations.QueryProjection;
import com.tastyhouse.core.domain.policy.domain.model.PolicyType;

import java.time.LocalDateTime;

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
    @QueryProjection
    public PolicyDocumentResult {
    }
}
