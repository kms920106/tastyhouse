package com.tastyhouse.core.domain.policy.application.dto.result;

import com.querydsl.core.annotations.QueryProjection;
import com.tastyhouse.core.domain.policy.domain.model.PolicyType;

import java.time.LocalDateTime;

public record PolicyListItemResult(
    Long id,
    PolicyType type,
    String version,
    String title,
    Boolean current,
    LocalDateTime effectiveDate,
    LocalDateTime createdAt
) {
    @QueryProjection
    public PolicyListItemResult {
    }
}
