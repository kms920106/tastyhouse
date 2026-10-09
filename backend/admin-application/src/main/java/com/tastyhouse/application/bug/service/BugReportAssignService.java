package com.tastyhouse.application.bug.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.admin.vo.AdminId;
import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.bug.vo.BugReportId;
import com.tastyhouse.application.bug.port.in.BugReportAssignCommand;
import com.tastyhouse.application.bug.port.in.BugReportAssignUseCase;
import com.tastyhouse.application.bug.port.out.write.BugReportLoadPort;
import com.tastyhouse.application.bug.port.out.write.BugReportSavePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class BugReportAssignService implements BugReportAssignUseCase {

    private final BugReportLoadPort bugReportLoadPort;
    private final BugReportSavePort bugReportSavePort;

    public BugReportAssignService(BugReportLoadPort bugReportLoadPort, BugReportSavePort bugReportSavePort) {
        this.bugReportLoadPort = bugReportLoadPort;
        this.bugReportSavePort = bugReportSavePort;
    }

    @Override
    public void assign(BugReportAssignCommand command) {
        BugReportId bugReportId = BugReportId.of(command.bugReportId());
        BugReport bugReport = findBugReportOrThrow(bugReportId);

        AdminId adminId = AdminId.of(command.assigneeAdminId());
        bugReport.assignTo(adminId);
        bugReportSavePort.save(bugReport);
    }

    private BugReport findBugReportOrThrow(BugReportId bugReportId) {
        return bugReportLoadPort.findById(bugReportId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.BUG_REPORT_NOT_FOUND));
    }
}
