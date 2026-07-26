package com.tastyhouse.webapi.bug;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.bug.domain.model.BugReportPlatform;
import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.bug.application.BugReportCommandService;
import com.tastyhouse.core.domain.bug.application.dto.command.BugReportCreateCommand;
import com.tastyhouse.core.domain.bug.application.dto.result.BugReportResult;
import com.tastyhouse.core.domain.file.application.FileQueryService;
import com.tastyhouse.webapi.bug.response.BugReportResponse;
import com.tastyhouse.webapi.file.FileService;

@Service
@RequiredArgsConstructor
public class BugReportService {

    private final BugReportCommandService bugReportCommandService;
    private final FileService fileService;
    private final FileQueryService fileQueryService;

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
        return toBugReportResponse(result);
    }

    private BugReportResponse toBugReportResponse(BugReportResult result) {
        return BugReportResponse.from(
            result.id().value(),
            result.device(),
            result.title(),
            result.content(),
            result.appVersion(),
            result.platform() != null ? result.platform().name() : null,
            result.osVersion(),
            result.status() != null ? result.status().name() : null,
            toImageUrls(result.uploadedFileIds()),
            result.createdAt()
        );
    }

    private List<String> toImageUrls(List<Long> imageFileIds) {
        if (imageFileIds == null || imageFileIds.isEmpty()) {
            return List.of();
        }
        return imageFileIds.stream()
            .map(fileId -> fileQueryService.findFilePath(UploadedFileId.of(fileId))
                .map(fileService::getUrlByPath)
                .orElse(null))
            .filter(Objects::nonNull)
            .toList();
    }
}
