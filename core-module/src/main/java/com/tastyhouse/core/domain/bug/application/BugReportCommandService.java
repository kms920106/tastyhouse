package com.tastyhouse.core.domain.bug.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.bug.application.dto.command.CreateBugReportCommand;
import com.tastyhouse.core.domain.bug.application.dto.result.BugReportResult;
import com.tastyhouse.core.domain.bug.domain.model.BugReport;
import com.tastyhouse.core.domain.bug.domain.model.BugReportImage;
import com.tastyhouse.core.domain.bug.domain.repository.BugReportImageRepository;
import com.tastyhouse.core.domain.bug.domain.repository.BugReportRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class BugReportCommandService {

    private final BugReportRepository bugReportRepository;
    private final BugReportImageRepository bugReportImageRepository;

    public BugReportResult create(CreateBugReportCommand command) {
        BugReport bugReport = BugReport.create(
            command.memberId(),
            command.device(),
            command.title(),
            command.content()
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
}
