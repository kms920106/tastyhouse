package com.tastyhouse.application.bug.port.out;

import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface BugReportQueryPort {

    PageResult<BugReportListItemResult> findBugReports(BugReportSearchCondition condition, PageQuery pageQuery);

    Optional<BugReportDetailResult> findDetailById(Long id);
}
