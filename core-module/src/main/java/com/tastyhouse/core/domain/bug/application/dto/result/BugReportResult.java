package com.tastyhouse.core.domain.bug.application.dto.result;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.core.domain.bug.domain.model.BugReport;
import com.tastyhouse.core.domain.bug.domain.model.BugReportPlatform;
import com.tastyhouse.core.domain.bug.domain.model.BugReportStatus;
import com.tastyhouse.core.domain.bug.domain.vo.BugReportId;

public record BugReportResult(
    BugReportId id,
    String device,
    String title,
    String content,
    String appVersion,
    BugReportPlatform platform,
    String osVersion,
    BugReportStatus status,
    List<Long> uploadedFileIds,
    LocalDateTime createdAt
) {
    public static BugReportResult from(BugReport bugReport, List<Long> uploadedFileIds) {
        return new BugReportResult(
            bugReport.getBugReportId(),
            bugReport.getDevice(),
            bugReport.getTitle(),
            bugReport.getContent(),
            bugReport.getAppVersion(),
            bugReport.getPlatform(),
            bugReport.getOsVersion(),
            bugReport.getStatus(),
            uploadedFileIds,
            bugReport.getCreatedAt()
        );
    }
}
