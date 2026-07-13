package com.tastyhouse.adminapi.bug;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.bug.domain.model.BugReportCategory;
import com.tastyhouse.core.domain.bug.domain.model.BugReportPriority;
import com.tastyhouse.core.domain.bug.domain.model.BugReportStatus;
import com.tastyhouse.core.domain.bug.domain.vo.BugReportId;
import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.bug.application.BugReportCommandService;
import com.tastyhouse.core.domain.bug.application.BugReportQueryService;
import com.tastyhouse.core.domain.bug.application.dto.BugReportAdminListItemDto;
import com.tastyhouse.core.domain.bug.application.dto.BugReportAdminSearchCondition;
import com.tastyhouse.core.domain.bug.application.dto.BugReportDetailDto;
import com.tastyhouse.core.domain.bug.application.dto.command.BugReportAssignCommand;
import com.tastyhouse.core.domain.bug.application.dto.command.BugReportClassifyCommand;
import com.tastyhouse.core.domain.bug.application.dto.command.BugReportStatusUpdateCommand;
import com.tastyhouse.core.domain.file.application.FileQueryService;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.member.application.dto.result.MemberWithProfileImageResult;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.adminapi.bug.response.BugReportDetailResponse;
import com.tastyhouse.adminapi.bug.response.BugReportListItemResponse;
import com.tastyhouse.adminapi.bug.response.BugReportPageResponse;
import com.tastyhouse.adminapi.bug.response.MemberSummaryResponse;
import com.tastyhouse.adminapi.common.FileResponse;

@Service
@RequiredArgsConstructor
public class BugReportService {

    private final BugReportQueryService bugReportQueryService;
    private final BugReportCommandService bugReportCommandService;
    private final MemberQueryService memberQueryService;
    private final FileQueryService fileQueryService;
    private final FileService fileService;

    public BugReportPageResponse getBugReports(
        String title,
        String content,
        Long memberId,
        String status,
        String category,
        String priority,
        int page,
        int size
    ) {
        BugReportAdminSearchCondition condition = BugReportAdminSearchCondition.of(
            title,
            content,
            memberId,
            status == null ? null : BugReportStatus.from(status),
            category == null ? null : BugReportCategory.from(category),
            priority == null ? null : BugReportPriority.from(priority)
        );
        PageResult<BugReportAdminListItemDto> pageResult = bugReportQueryService.findAllForAdmin(condition, page, size);

        Map<Long, MemberWithProfileImageResult> membersById = memberQueryService.findMemberWithProfileImagesByIds(
            pageResult.content().stream().map(BugReportAdminListItemDto::memberId).toList()
        );

        PageResult<BugReportListItemResponse> responsePage = pageResult.map(
            dto -> BugReportListItemResponse.from(dto, MemberSummaryResponse.from(membersById.get(dto.memberId())))
        );
        return BugReportPageResponse.from(responsePage);
    }

    public BugReportDetailResponse getBugReport(Long id) {
        BugReportDetailDto detail = bugReportQueryService.findDetailById(BugReportId.of(id));

        MemberSummaryResponse member = memberQueryService.findMemberWithProfileImage(MemberId.of(detail.memberId()))
            .map(MemberSummaryResponse::from)
            .orElse(null);

        List<FileResponse> images = toFileResponses(detail.imageFileIds());

        return BugReportDetailResponse.from(detail, member, images);
    }

    public void changeStatus(Long id, String status, String answer) {
        bugReportCommandService.changeStatus(
            BugReportStatusUpdateCommand.of(BugReportId.of(id), BugReportStatus.from(status), answer)
        );
    }

    public void classify(Long id, String category, String priority) {
        bugReportCommandService.classify(
            BugReportClassifyCommand.of(BugReportId.of(id), BugReportCategory.from(category), BugReportPriority.from(priority))
        );
    }

    public void assign(Long id, Long assigneeAdminId) {
        bugReportCommandService.assign(
            BugReportAssignCommand.of(BugReportId.of(id), assigneeAdminId)
        );
    }

    private List<FileResponse> toFileResponses(List<Long> imageFileIds) {
        if (imageFileIds == null || imageFileIds.isEmpty()) {
            return Collections.emptyList();
        }
        return imageFileIds.stream()
            .map(fileId -> fileQueryService.findById(UploadedFileId.of(fileId))
                .map(file -> FileResponse.of(file.getId(), file.getOriginalFilename(), fileService.getUrlByPath(file.getFilePath())))
                .orElse(null))
            .filter(Objects::nonNull)
            .toList();
    }
}
