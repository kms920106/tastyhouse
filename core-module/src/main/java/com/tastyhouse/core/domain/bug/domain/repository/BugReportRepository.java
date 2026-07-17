package com.tastyhouse.core.domain.bug.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.bug.domain.model.BugReport;
import com.tastyhouse.core.domain.bug.domain.vo.BugReportId;
import com.tastyhouse.core.domain.bug.application.dto.BugReportSearchCondition;
import com.tastyhouse.core.domain.bug.application.dto.result.BugReportListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface BugReportRepository {

    PageResult<BugReportListItemResult> findAllBugReports(BugReportSearchCondition condition, PageQuery pageQuery);

    Optional<BugReport> findById(BugReportId bugReportId);

    BugReport save(BugReport bugReport);
}
