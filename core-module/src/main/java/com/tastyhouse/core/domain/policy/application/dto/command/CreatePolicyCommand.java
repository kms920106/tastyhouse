package com.tastyhouse.core.domain.policy.application.dto.command;

import com.tastyhouse.core.domain.policy.domain.model.PolicyType;

import java.time.LocalDateTime;

public record CreatePolicyCommand(
    PolicyType type,
    String version,
    String title,
    String content,
    Boolean mandatory,
    LocalDateTime effectiveDate,
    String createdBy
) {}
