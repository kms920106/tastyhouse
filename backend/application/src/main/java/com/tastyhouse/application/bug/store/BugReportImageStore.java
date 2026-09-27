package com.tastyhouse.application.bug.store;

import com.tastyhouse.domain.bug.model.BugReportImage;
import com.tastyhouse.application.bug.port.out.write.BugReportImageStatePort;

public class BugReportImageStore implements BugReportImageRepository {
    private final BugReportImageStatePort bugReportImageStatePort;

    public BugReportImageStore(BugReportImageStatePort bugReportImageStatePort) {
        this.bugReportImageStatePort = bugReportImageStatePort;
    }

    @Override
    public BugReportImage save(BugReportImage bugReportImage) {
        return BugReportImageStateMapper.toDomain(
            bugReportImageStatePort.save(BugReportImageStateMapper.toState(bugReportImage)));
    }
}
