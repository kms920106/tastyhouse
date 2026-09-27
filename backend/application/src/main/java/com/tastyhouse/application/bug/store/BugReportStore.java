package com.tastyhouse.application.bug.store;

import java.util.Optional;

import com.tastyhouse.application.bug.port.out.write.BugReportStatePort;
import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.bug.vo.BugReportId;

public class BugReportStore implements BugReportRepository {
    private final BugReportStatePort bugReportStatePort;

    public BugReportStore(BugReportStatePort bugReportStatePort) {
        this.bugReportStatePort = bugReportStatePort;
    }

    @Override
    public Optional<BugReport> findById(BugReportId bugReportId) {
        if (bugReportId == null) {
            return Optional.empty();
        }
        return bugReportStatePort.findById(bugReportId.value()).map(BugReportStateMapper::toDomain);
    }

    @Override
    public BugReport save(BugReport bugReport) {
        return BugReportStateMapper.toDomain(bugReportStatePort.save(BugReportStateMapper.toState(bugReport)));
    }
}
