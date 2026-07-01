package com.tastyhouse.core.domain.policy.application.dto.result;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.policy.domain.model.PolicyType;

public record PolicyListItemResult(
    Long id,
    PolicyType type,
    String version,
    String title,
    boolean current,
    LocalDateTime effectiveDate,
    LocalDateTime createdAt
) {
    @QueryProjection
    public PolicyListItemResult {
    }
}
