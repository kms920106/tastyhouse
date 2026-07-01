package com.tastyhouse.webapi.policy.response;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.policy.domain.model.PolicyType;

public record PolicyDetailResponse(
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
    public static PolicyDetailResponse from(
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
