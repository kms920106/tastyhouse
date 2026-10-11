package com.tastyhouse.application.bug.port.out;

import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface BugReportQueryPort {

    PageResult<BugReportListItemResult> findBugReports(BugReportSearchCondition condition, PageQuery pageQuery);

    Optional<BugReportDetailResult> findDetailById(Long id);
}
