package com.tastyhouse.application.bug.port.out.write;

import com.tastyhouse.domain.bug.model.BugReport;

public interface BugReportSavePort {

    BugReport save(BugReport bugReport);
}
