package com.tastyhouse.application.bug.port.out.write;

import com.tastyhouse.domain.bug.model.BugReportImage;

public interface BugReportImageRepository {
    BugReportImage save(BugReportImage bugReportImage);
}
