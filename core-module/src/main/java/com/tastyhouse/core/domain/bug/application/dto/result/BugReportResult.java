package com.tastyhouse.core.domain.bug.application.dto.result;

import com.tastyhouse.core.domain.bug.domain.model.BugReport;

import java.time.LocalDateTime;
import java.util.List;

public record BugReportResult(
    Long id,
    String device,
    String title,
    String content,
    List<Long> uploadedFileIds,
    LocalDateTime createdAt
) {
    public static BugReportResult from(BugReport bugReport, List<Long> uploadedFileIds) {
        return new BugReportResult(
            bugReport.getId(),
            bugReport.getDevice(),
            bugReport.getTitle(),
            bugReport.getContent(),
            uploadedFileIds,
            bugReport.getCreatedAt()
        );
    }
}
