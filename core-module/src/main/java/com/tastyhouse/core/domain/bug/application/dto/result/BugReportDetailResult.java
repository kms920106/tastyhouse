package com.tastyhouse.core.domain.bug.application.dto.result;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.core.domain.bug.domain.model.BugReportCategory;
import com.tastyhouse.core.domain.bug.domain.model.BugReportPlatform;
import com.tastyhouse.core.domain.bug.domain.model.BugReportPriority;
import com.tastyhouse.core.domain.bug.domain.model.BugReportStatus;
import com.tastyhouse.core.domain.bug.domain.vo.BugReportId;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record BugReportDetailResult(
    BugReportId id,
    MemberId memberId,
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

    public static BugReportDetailResult from(
        BugReportId id,
        MemberId memberId,
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
        return new BugReportDetailResult(
            id, memberId, device, title, content,
            status, category, priority, assigneeAdminId, adminAnswer, resolvedAt,
            appVersion, platform, osVersion,
            imageFileIds, createdAt, updatedAt
        );
    }
}
