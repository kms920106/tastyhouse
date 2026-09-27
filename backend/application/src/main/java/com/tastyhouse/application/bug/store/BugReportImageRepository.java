package com.tastyhouse.application.bug.store;

import com.tastyhouse.domain.bug.model.BugReportImage;

public interface BugReportImageRepository {
    BugReportImage save(BugReportImage bugReportImage);
}
