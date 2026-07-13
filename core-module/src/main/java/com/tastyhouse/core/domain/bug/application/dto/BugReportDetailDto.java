package com.tastyhouse.core.domain.bug.application.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.core.domain.bug.domain.model.BugReportCategory;
import com.tastyhouse.core.domain.bug.domain.model.BugReportPlatform;
import com.tastyhouse.core.domain.bug.domain.model.BugReportPriority;
import com.tastyhouse.core.domain.bug.domain.model.BugReportStatus;
import com.tastyhouse.core.domain.bug.domain.vo.BugReportId;

public record BugReportDetailDto(
    BugReportId id,
    Long memberId,
    String device,
    String title,
    String content,
    BugReportStatus status,
    BugReportCategory category,
    BugReportPriority priority,
    Long assigneeAdminId,
    String adminAnswer,
    LocalDateTime resolvedAt,
    String appVersion,
    BugReportPlatform platform,
    String osVersion,
    List<Long> imageFileIds,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static BugReportDetailDto from(
        BugReportId id,
        Long memberId,
        String device,
        String title,
        String content,
        BugReportStatus status,
        BugReportCategory category,
        BugReportPriority priority,
        Long assigneeAdminId,
        String adminAnswer,
        LocalDateTime resolvedAt,
        String appVersion,
        BugReportPlatform platform,
        String osVersion,
        List<Long> imageFileIds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new BugReportDetailDto(
            id, memberId, device, title, content,
            status, category, priority, assigneeAdminId, adminAnswer, resolvedAt,
            appVersion, platform, osVersion,
            imageFileIds, createdAt, updatedAt
        );
    }
}
