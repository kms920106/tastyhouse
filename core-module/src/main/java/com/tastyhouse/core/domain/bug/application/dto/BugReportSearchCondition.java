package com.tastyhouse.core.domain.bug.application.dto;

import com.tastyhouse.core.domain.bug.domain.model.BugReportCategory;
import com.tastyhouse.core.domain.bug.domain.model.BugReportPriority;
import com.tastyhouse.core.domain.bug.domain.model.BugReportStatus;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record BugReportSearchCondition(
    String title,
    String content,
    MemberId memberId,
    BugReportStatus status,
    BugReportCategory category,
    BugReportPriority priority
) {

    public static BugReportSearchCondition of(
        String title,
        String content,
        MemberId memberId,
        BugReportStatus status,
        BugReportCategory category,
        BugReportPriority priority
    ) {
        return new BugReportSearchCondition(title, content, memberId, status, category, priority);
    }
}
