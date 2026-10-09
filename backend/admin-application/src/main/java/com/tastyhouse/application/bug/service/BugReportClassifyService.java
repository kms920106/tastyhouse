package com.tastyhouse.application.bug.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.vo.BugReportId;
import com.tastyhouse.application.bug.port.in.BugReportClassifyCommand;
import com.tastyhouse.application.bug.port.in.BugReportClassifyUseCase;
import com.tastyhouse.application.bug.port.out.write.BugReportLoadPort;
import com.tastyhouse.application.bug.port.out.write.BugReportSavePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class BugReportClassifyService implements BugReportClassifyUseCase {

    private final BugReportLoadPort bugReportLoadPort;
    private final BugReportSavePort bugReportSavePort;

    public BugReportClassifyService(BugReportLoadPort bugReportLoadPort, BugReportSavePort bugReportSavePort) {
        this.bugReportLoadPort = bugReportLoadPort;
        this.bugReportSavePort = bugReportSavePort;
    }

    @Override
    public void classify(BugReportClassifyCommand command) {
        BugReportId bugReportId = BugReportId.of(command.bugReportId());
        BugReportCategory bugReportCategory = BugReportCategory.from(command.category());
        BugReportPriority bugReportPriority = BugReportPriority.from(command.priority());
        BugReport bugReport = findBugReportOrThrow(bugReportId);

        bugReport.classify(bugReportCategory, bugReportPriority);
        bugReportSavePort.save(bugReport);
    }

    private BugReport findBugReportOrThrow(BugReportId bugReportId) {
        return bugReportLoadPort.findById(bugReportId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.BUG_REPORT_NOT_FOUND));
    }
}
