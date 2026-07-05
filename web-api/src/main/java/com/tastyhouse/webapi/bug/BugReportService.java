package com.tastyhouse.webapi.bug;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.bug.application.BugReportCommandService;
import com.tastyhouse.core.domain.bug.application.dto.command.CreateBugReportCommand;
import com.tastyhouse.core.domain.bug.application.dto.result.BugReportResult;
import com.tastyhouse.webapi.bug.response.BugReportResponse;

@Service
@RequiredArgsConstructor
public class BugReportService {

    private final BugReportCommandService bugReportCommandService;

    public BugReportResponse createBugReport(Long memberId, String device, String title, String content, List<Long> uploadedFileIds) {
        CreateBugReportCommand command = CreateBugReportCommand.of(memberId, device, title, content, uploadedFileIds);
        BugReportResult result = bugReportCommandService.create(command);
        return BugReportResponse.from(result);
    }
}
