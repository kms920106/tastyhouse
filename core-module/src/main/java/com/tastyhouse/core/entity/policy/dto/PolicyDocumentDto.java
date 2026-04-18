package com.tastyhouse.core.entity.policy.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.tastyhouse.core.entity.policy.PolicyType;

import java.time.LocalDateTime;

public record PolicyDocumentDto(
    Long id,
    PolicyType type,
    String version,
    String title,
    String content,
    Boolean current,
    Boolean mandatory,
    LocalDateTime effectiveDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    @QueryProjection
    public PolicyDocumentDto {
    }
}
