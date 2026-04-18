package com.tastyhouse.core.entity.policy.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.tastyhouse.core.entity.policy.PolicyType;

import java.time.LocalDateTime;

public record PolicyListItemDto(
    Long id,
    PolicyType type,
    String version,
    String title,
    Boolean current,
    LocalDateTime effectiveDate,
    LocalDateTime createdAt
) {
    @QueryProjection
    public PolicyListItemDto {
    }
}
