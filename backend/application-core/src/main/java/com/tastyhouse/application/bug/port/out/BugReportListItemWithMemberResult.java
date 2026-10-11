package com.tastyhouse.application.bug.port.out;

import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;

public record BugReportListItemWithMemberResult(
    BugReportListItemResult bugReport,
    MemberWithProfileImageResult member
) {
}
