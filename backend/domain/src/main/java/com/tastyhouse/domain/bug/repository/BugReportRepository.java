package com.tastyhouse.domain.bug.repository;

import java.util.Optional;

import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.bug.vo.BugReportId;

public interface BugReportRepository {
    Optional<BugReport> findById(BugReportId bugReportId);

    BugReport save(BugReport bugReport);
}
