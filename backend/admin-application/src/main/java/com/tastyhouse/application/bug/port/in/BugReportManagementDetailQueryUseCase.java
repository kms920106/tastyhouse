package com.tastyhouse.application.bug.port.in;

import com.tastyhouse.application.bug.port.out.BugReportDetailWithMemberResult;

public interface BugReportManagementDetailQueryUseCase {

    BugReportDetailWithMemberResult getBugReport(Long id);
}
