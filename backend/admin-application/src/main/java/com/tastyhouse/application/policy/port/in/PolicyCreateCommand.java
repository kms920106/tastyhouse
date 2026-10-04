package com.tastyhouse.application.policy.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record PolicyCreateCommand(
    String type,
    String version,
    String title,
    String content,
    boolean mandatory,
    LocalDateTime effectiveDate,
    String createdBy
) {

    public PolicyCreateCommand {
        if (type == null || version == null || title == null || content == null || effectiveDate == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
