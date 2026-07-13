package com.tastyhouse.core.domain.bug.application.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.bug.domain.model.BugReportCategory;
import com.tastyhouse.core.domain.bug.domain.model.BugReportPriority;
import com.tastyhouse.core.domain.bug.domain.model.BugReportStatus;

public record BugReportAdminListItemDto(
    Long id,
    Long memberId,
    String device,
    String title,
    BugReportStatus status,
    BugReportCategory category,
    BugReportPriority priority,
    long imageCount,
    LocalDateTime createdAt
) {
    @QueryProjection
    public BugReportAdminListItemDto {
    }
}
