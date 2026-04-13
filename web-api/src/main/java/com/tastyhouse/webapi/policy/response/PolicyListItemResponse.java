package com.tastyhouse.webapi.policy.response;

import com.tastyhouse.core.entity.policy.PolicyType;

import java.time.LocalDateTime;

public record PolicyListItemResponse(
    Long id,
    PolicyType type,
    String version,
    String title,
    Boolean current,
    LocalDateTime effectiveDate,
    LocalDateTime createdAt
) {
    public static PolicyListItemResponse from(
        Long id,
        PolicyType type,
        String version,
        String title,
        Boolean current,
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
