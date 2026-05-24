package com.tastyhouse.core.domain.policy.application.dto.command;

import java.time.LocalDateTime;

public record UpdatePolicyCommand(
    String title,
    String content,
    Boolean mandatory,
    LocalDateTime effectiveDate,
    String updatedBy
) {}
