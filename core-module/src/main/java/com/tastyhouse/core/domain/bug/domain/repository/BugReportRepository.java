package com.tastyhouse.core.domain.bug.domain.repository;

import com.tastyhouse.core.domain.bug.domain.model.BugReport;

public interface BugReportRepository {

    BugReport save(BugReport bugReport);
}
