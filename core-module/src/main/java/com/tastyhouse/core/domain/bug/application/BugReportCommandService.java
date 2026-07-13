package com.tastyhouse.core.domain.bug.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.bug.domain.model.BugReport;
import com.tastyhouse.core.domain.bug.domain.model.BugReportImage;
import com.tastyhouse.core.domain.bug.domain.repository.BugReportImageRepository;
import com.tastyhouse.core.domain.bug.domain.repository.BugReportRepository;
import com.tastyhouse.core.domain.bug.domain.vo.BugReportId;
import com.tastyhouse.core.domain.bug.application.dto.command.BugReportAssignCommand;
import com.tastyhouse.core.domain.bug.application.dto.command.BugReportClassifyCommand;
import com.tastyhouse.core.domain.bug.application.dto.command.BugReportCreateCommand;
import com.tastyhouse.core.domain.bug.application.dto.command.BugReportStatusUpdateCommand;
import com.tastyhouse.core.domain.bug.application.dto.result.BugReportResult;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class BugReportCommandService {

    private final BugReportRepository bugReportRepository;
    private final BugReportImageRepository bugReportImageRepository;

    public BugReportResult create(BugReportCreateCommand command) {
        BugReport bugReport = BugReport.create(
            command.memberId(),
            command.device(),
            command.title(),
            command.content(),
            command.appVersion(),
            command.platform(),
            command.osVersion()
        );
        BugReport saved = bugReportRepository.save(bugReport);

        List<Long> uploadedFileIds = command.uploadedFileIds();
        if (uploadedFileIds != null && !uploadedFileIds.isEmpty()) {
            for (int i = 0; i < uploadedFileIds.size(); i++) {
                bugReportImageRepository.save(
                    BugReportImage.create(saved.getId(), uploadedFileIds.get(i), i)
                );
            }
        }

        return BugReportResult.from(saved, uploadedFileIds);
    }

    public void changeStatus(BugReportStatusUpdateCommand command) {
        BugReport bugReport = findById(command.id());

        switch (command.status()) {
            case IN_PROGRESS -> bugReport.startProgress();
            case RESOLVED -> bugReport.resolve(command.answer());
            case REJECTED -> bugReport.reject(command.answer());
            case ON_HOLD -> bugReport.hold();
            case RECEIVED -> throw new BusinessException(ErrorCode.BUG_REPORT_INVALID_STATUS);
        }
    }

    public void classify(BugReportClassifyCommand command) {
        BugReport bugReport = findById(command.id());
        bugReport.classify(command.category(), command.priority());
    }

    public void assign(BugReportAssignCommand command) {
        BugReport bugReport = findById(command.id());
        bugReport.assignTo(command.assigneeAdminId());
    }

    private BugReport findById(BugReportId id) {
        return bugReportRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BUG_REPORT_NOT_FOUND));
    }
}
