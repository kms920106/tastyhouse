package com.tastyhouse.core.domain.bug.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.bug.domain.model.BugReport;
import com.tastyhouse.core.domain.bug.domain.vo.BugReportId;
import com.tastyhouse.core.domain.bug.application.dto.BugReportAdminListItemDto;
import com.tastyhouse.core.domain.bug.application.dto.BugReportAdminSearchCondition;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface BugReportRepository {

    PageResult<BugReportAdminListItemDto> findAllBugReports(BugReportAdminSearchCondition condition, PageQuery pageQuery);

    Optional<BugReport> findById(BugReportId bugReportId);

    BugReport save(BugReport bugReport);
}
