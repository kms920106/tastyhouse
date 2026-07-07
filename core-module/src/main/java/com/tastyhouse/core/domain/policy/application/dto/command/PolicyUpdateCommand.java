package com.tastyhouse.core.domain.policy.application.dto.command;

import java.time.LocalDateTime;

public record PolicyUpdateCommand(
    String title,
    String content,
    boolean mandatory,
    LocalDateTime effectiveDate,
    String updatedBy
) {

    public static PolicyUpdateCommand of(
        String title,
        String content,
        boolean mandatory,
        LocalDateTime effectiveDate,
        String updatedBy
    ) {
        return new PolicyUpdateCommand(title, content, mandatory, effectiveDate, updatedBy);
    }
}
