package com.tastyhouse.core.domain.bug.application.dto;

import com.tastyhouse.core.domain.bug.domain.model.BugReportCategory;
import com.tastyhouse.core.domain.bug.domain.model.BugReportPriority;
import com.tastyhouse.core.domain.bug.domain.model.BugReportStatus;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record BugReportAdminSearchCondition(
    String title,
    String content,
    MemberId memberId,
    BugReportStatus status,
    BugReportCategory category,
    BugReportPriority priority
) {

    public static BugReportAdminSearchCondition of(
        String title,
        String content,
        MemberId memberId,
        BugReportStatus status,
        BugReportCategory category,
        BugReportPriority priority
    ) {
        return new BugReportAdminSearchCondition(title, content, memberId, status, category, priority);
    }
}
