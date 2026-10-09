package com.tastyhouse.application.bug.port.out.write;

import com.tastyhouse.domain.bug.model.BugReportImage;

public interface BugReportImageSavePort {

    BugReportImage save(BugReportImage bugReportImage);
}
