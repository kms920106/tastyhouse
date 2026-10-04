package com.tastyhouse.application.bug.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record BugReportCreateCommand(
    Long reporterId,
    String device,
    String title,
    String content,
    String appVersion,
    String platform,
    String osVersion,
    List<Long> uploadedFileIds
) {

    public BugReportCreateCommand {
        if (reporterId == null || device == null || title == null || content == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
