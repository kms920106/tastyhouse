package com.tastyhouse.application.bug.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.bug.model.BugReportStatus;
import com.tastyhouse.domain.bug.vo.BugReportId;
import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.application.bug.port.in.BugReportManagementStatusChangeUseCase;
import com.tastyhouse.application.bug.port.in.BugReportStatusChangeCommand;
import com.tastyhouse.application.bug.port.out.write.BugReportLoadPort;
import com.tastyhouse.application.bug.port.out.write.BugReportSavePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class BugReportManagementStatusChangeService implements BugReportManagementStatusChangeUseCase {

    private final BugReportLoadPort bugReportLoadPort;
    private final BugReportSavePort bugReportSavePort;

    public BugReportManagementStatusChangeService(BugReportLoadPort bugReportLoadPort, BugReportSavePort bugReportSavePort) {
        this.bugReportLoadPort = bugReportLoadPort;
        this.bugReportSavePort = bugReportSavePort;
    }

    @Override
    public void changeStatus(BugReportStatusChangeCommand command) {
        String answer = command.answer();
        BugReportId bugReportId = BugReportId.of(command.bugReportId());
        BugReportStatus bugReportStatus = BugReportStatus.from(command.status());
        BugReport bugReport = findBugReportOrThrow(bugReportId);

        switch (bugReportStatus) {
            case IN_PROGRESS -> bugReport.startProgress();
            case RESOLVED -> bugReport.resolve(answer);
            case REJECTED -> bugReport.reject(answer);
            case ON_HOLD -> bugReport.hold();
            case RECEIVED -> throw new DomainException(DomainErrorCode.BUG_REPORT_INVALID_STATUS);
        }

        bugReportSavePort.save(bugReport);
    }

    private BugReport findBugReportOrThrow(BugReportId bugReportId) {
        return bugReportLoadPort.findById(bugReportId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.BUG_REPORT_NOT_FOUND));
    }
}
