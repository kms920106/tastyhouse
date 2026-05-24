package com.tastyhouse.webapi.policy.response;

import com.tastyhouse.core.domain.policy.domain.model.PolicyType;

import java.time.LocalDateTime;

public record PolicyDetailResponse(
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
    public static PolicyDetailResponse from(
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
    return new PolicyDetailResponse(
        id,
        type,
        version,
        title,
        content,
        current,
        mandatory,
        effectiveDate,
        createdAt,
        updatedAt
    );
    }
}
