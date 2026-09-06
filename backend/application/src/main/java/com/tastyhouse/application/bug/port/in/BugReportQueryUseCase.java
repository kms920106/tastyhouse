package com.tastyhouse.application.bug.port.in;

import com.tastyhouse.application.bug.port.out.BugReportDetailWithMemberResult;
import com.tastyhouse.application.bug.port.out.BugReportListItemWithMemberResult;
import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.domain.shared.page.PageResult;

@AdminApp
public interface BugReportQueryUseCase {

    PageResult<BugReportListItemWithMemberResult> getBugReports(
        String title,
        String content,
        Long memberId,
        String status,
        String category,
        String priority,
        int page,
        int size
    );

    BugReportDetailWithMemberResult getBugReport(Long id);
}
