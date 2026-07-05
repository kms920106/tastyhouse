package com.tastyhouse.core.domain.policy.application.dto.command;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.policy.domain.model.PolicyType;

public record CreatePolicyCommand(
    PolicyType type,
    String version,
    String title,
    String content,
    boolean mandatory,
    LocalDateTime effectiveDate,
    String createdBy
) {

    public static CreatePolicyCommand of(
        PolicyType type,
        String version,
        String title,
        String content,
        boolean mandatory,
        LocalDateTime effectiveDate,
        String createdBy
    ) {
        return new CreatePolicyCommand(type, version, title, content, mandatory, effectiveDate, createdBy);
    }
}
