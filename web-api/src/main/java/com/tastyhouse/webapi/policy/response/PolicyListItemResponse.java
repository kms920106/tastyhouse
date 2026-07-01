package com.tastyhouse.webapi.policy.response;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.policy.domain.model.PolicyType;

public record PolicyListItemResponse(
    Long id,
    PolicyType type,
    String version,
    String title,
    boolean current,
    LocalDateTime effectiveDate,
    LocalDateTime createdAt
) {
    public static PolicyListItemResponse from(
        Long id,
        PolicyType type,
        String version,
        String title,
        boolean current,
        LocalDateTime effectiveDate,
        LocalDateTime createdAt
    ) {
        return new PolicyListItemResponse(
            id,
            type,
            version,
            title,
            current,
            effectiveDate,
            createdAt
        );
    }
}
