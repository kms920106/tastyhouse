package com.tastyhouse.webapi.bug;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.bug.domain.model.BugReportPlatform;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.bug.application.BugReportCommandService;
import com.tastyhouse.core.domain.bug.application.dto.command.BugReportCreateCommand;
import com.tastyhouse.core.domain.bug.application.dto.result.BugReportResult;
import com.tastyhouse.webapi.bug.response.BugReportResponse;

@Service
@RequiredArgsConstructor
public class BugReportService {

    private final BugReportCommandService bugReportCommandService;

    public BugReportResponse createBugReport(
        Long memberId,
        String device,
        String title,
        String content,
        String appVersion,
        String platform,
        String osVersion,
        List<Long> uploadedFileIds
    ) {
        BugReportCreateCommand command = BugReportCreateCommand.of(
            MemberId.of(memberId),
            device,
            title,
            content,
            appVersion,
            platform == null ? null : BugReportPlatform.from(platform),
            osVersion,
            uploadedFileIds
        );
        BugReportResult result = bugReportCommandService.create(command);
        return BugReportResponse.from(result);
    }
}
