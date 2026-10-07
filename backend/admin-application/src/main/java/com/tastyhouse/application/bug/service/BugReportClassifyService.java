package com.tastyhouse.application.bug.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.vo.BugReportId;
import com.tastyhouse.application.bug.port.in.BugReportClassifyCommand;
import com.tastyhouse.application.bug.port.in.BugReportClassifyUseCase;
import com.tastyhouse.application.bug.port.out.write.BugReportPersistencePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class BugReportClassifyService implements BugReportClassifyUseCase {

    private final BugReportPersistencePort bugReportPersistencePort;

    public BugReportClassifyService(BugReportPersistencePort bugReportPersistencePort) {
        this.bugReportPersistencePort = bugReportPersistencePort;
    }

    @Override
    public void classify(BugReportClassifyCommand command) {
        BugReportId bugReportId = BugReportId.of(command.bugReportId());
        BugReportCategory bugReportCategory = BugReportCategory.from(command.category());
        BugReportPriority bugReportPriority = BugReportPriority.from(command.priority());
        BugReport bugReport = findBugReportOrThrow(bugReportId);

        bugReport.classify(bugReportCategory, bugReportPriority);
        bugReportPersistencePort.save(bugReport);
    }

    private BugReport findBugReportOrThrow(BugReportId bugReportId) {
        return bugReportPersistencePort.findById(bugReportId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.BUG_REPORT_NOT_FOUND));
    }
}
