package com.tastyhouse.application.bug.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.bug.port.in.BugReportAssignCommand;
import com.tastyhouse.application.bug.port.in.BugReportClassifyCommand;
import com.tastyhouse.application.bug.port.in.BugReportManagementCommandUseCase;
import com.tastyhouse.application.bug.port.in.BugReportStatusChangeCommand;
import com.tastyhouse.domain.admin.vo.AdminId;
import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;
import com.tastyhouse.domain.bug.repository.BugReportRepository;
import com.tastyhouse.domain.bug.vo.BugReportId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

@Service
@AdminApp
@Transactional
public class BugReportManagementCommandService implements BugReportManagementCommandUseCase {

    private final BugReportRepository bugReportRepository;

    public BugReportManagementCommandService(BugReportRepository bugReportRepository) {
        this.bugReportRepository = bugReportRepository;
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
            case RECEIVED -> throw new BusinessException(ErrorCode.BUG_REPORT_INVALID_STATUS);
        }

        bugReportRepository.save(bugReport);
    }

    @Override
    public void classify(BugReportClassifyCommand command) {
        BugReportId bugReportId = BugReportId.of(command.bugReportId());
        BugReportCategory bugReportCategory = BugReportCategory.from(command.category());
        BugReportPriority bugReportPriority = BugReportPriority.from(command.priority());
        BugReport bugReport = findBugReportOrThrow(bugReportId);

        bugReport.classify(bugReportCategory, bugReportPriority);
        bugReportRepository.save(bugReport);
    }

    @Override
    public void assign(BugReportAssignCommand command) {
        BugReportId bugReportId = BugReportId.of(command.bugReportId());
        BugReport bugReport = findBugReportOrThrow(bugReportId);

        AdminId adminId = AdminId.of(command.assigneeAdminId());
        bugReport.assignTo(adminId);
        bugReportRepository.save(bugReport);
    }

    private BugReport findBugReportOrThrow(BugReportId bugReportId) {
        return bugReportRepository.findById(bugReportId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BUG_REPORT_NOT_FOUND));
    }
}
