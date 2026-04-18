package com.tastyhouse.webapi.bug.response;

import java.time.LocalDateTime;
import java.util.List;

public record BugReportResponse(
    Long id,
    String device,
    String title,
    String content,
    List<Long> uploadedFileIds,
    LocalDateTime createdAt
) {
    public static BugReportResponse from(
    Long id,
    String device,
    String title,
    String content,
    List<Long> uploadedFileIds,
    LocalDateTime createdAt
    ) {
    return new BugReportResponse(
        id,
        device,
        title,
        content,
        uploadedFileIds,
        createdAt
    );
    }
}
