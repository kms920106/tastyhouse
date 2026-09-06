package com.tastyhouse.application.bug.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.bug.model.BugReportPlatform;
import com.tastyhouse.domain.bug.service.BugReportRegistrationService;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.bug.port.in.BugReportCommandUseCase;
import com.tastyhouse.application.bug.port.in.BugReportCreateCommand;

@Service
@WebApp
@Transactional
public class BugReportCommandService implements BugReportCommandUseCase {

    private final BugReportRegistrationService bugReportRegistrationService;

    public BugReportCommandService(BugReportRegistrationService bugReportRegistrationService) {
        this.bugReportRegistrationService = bugReportRegistrationService;
    }

    @Override
    public Long createBugReport(BugReportCreateCommand command) {
        String platform = command.platform();

        MemberId reporterId = MemberId.of(command.reporterId());
        BugReportPlatform bugReportPlatform = platform == null ? null : BugReportPlatform.from(platform);

        BugReport bugReport = bugReportRegistrationService.register(
            reporterId,
            command.device(),
            command.title(),
            command.content(),
            command.appVersion(),
            bugReportPlatform,
            command.osVersion(),
            command.uploadedFileIds()
        );

        return bugReport.getBugReportId().value();
    }
}
