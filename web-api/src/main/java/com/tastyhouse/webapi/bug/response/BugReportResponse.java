package com.tastyhouse.webapi.bug.response;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.core.domain.bug.application.dto.result.BugReportResult;

public record BugReportResponse(
    Long id,
    String device,
    String title,
    String content,
    List<Long> uploadedFileIds,
    LocalDateTime createdAt
) {
    public static BugReportResponse from(BugReportResult result) {
        return new BugReportResponse(
            result.id().value(),
            result.device(),
            result.title(),
            result.content(),
            result.uploadedFileIds(),
            result.createdAt()
        );
    }
}
