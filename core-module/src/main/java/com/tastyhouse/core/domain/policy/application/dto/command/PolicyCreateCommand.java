package com.tastyhouse.core.domain.policy.application.dto.command;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.policy.domain.model.PolicyType;

public record PolicyCreateCommand(
    PolicyType type,
    String version,
    String title,
    String content,
    boolean mandatory,
    LocalDateTime effectiveDate,
    String createdBy
) {

    public static PolicyCreateCommand of(
        PolicyType type,
        String version,
        String title,
        String content,
        boolean mandatory,
        LocalDateTime effectiveDate,
        String createdBy
    ) {
        return new PolicyCreateCommand(type, version, title, content, mandatory, effectiveDate, createdBy);
    }
}
