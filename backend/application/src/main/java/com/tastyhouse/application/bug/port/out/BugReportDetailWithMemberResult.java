package com.tastyhouse.application.bug.port.out;

import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;

public record BugReportDetailWithMemberResult(
    BugReportDetailResult bugReport,
    MemberWithProfileImageResult member
) {
}
