package com.tastyhouse.application.bug.port.in;

import com.tastyhouse.application.bug.port.out.BugReportListItemWithMemberResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface BugReportManagementListQueryUseCase {

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
}
