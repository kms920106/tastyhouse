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
import com.tastyhouse.core.domain.bug.application.dto.BugReportSearchCondition;
import com.tastyhouse.core.domain.bug.application.dto.result.BugReportDetailResult;
import com.tastyhouse.core.domain.bug.application.dto.result.BugReportListItemResult;
import com.tastyhouse.core.domain.bug.application.dto.command.BugReportAssignCommand;
import com.tastyhouse.core.domain.bug.application.dto.command.BugReportClassifyCommand;
import com.tastyhouse.core.domain.bug.application.dto.command.BugReportStatusUpdateCommand;
import com.tastyhouse.core.domain.file.application.FileQueryService;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.member.application.dto.result.MemberWithProfileImageResult;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.adminapi.common.FileResponse;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.bug.response.BugReportDetailResponse;
import com.tastyhouse.adminapi.bug.response.BugReportListItemResponse;
import com.tastyhouse.adminapi.bug.response.MemberSummaryResponse;

@Service
@RequiredArgsConstructor
public class BugReportService {

    private final BugReportQueryService bugReportQueryService;
    private final BugReportCommandService bugReportCommandService;
    private final MemberQueryService memberQueryService;
    private final FileQueryService fileQueryService;
    private final FileService fileService;

    public PaginationResponse<BugReportListItemResponse> getBugReports(
        String title,
        String content,
        Long memberId,
        String status,
        String category,
        String priority,
        int page,
        int size
    ) {
        BugReportSearchCondition condition = BugReportSearchCondition.of(
            title,
            content,
            memberId == null ? null : MemberId.of(memberId),
            status == null ? null : BugReportStatus.from(status),
            category == null ? null : BugReportCategory.from(category),
            priority == null ? null : BugReportPriority.from(priority)
        );
        PageResult<BugReportListItemResult> pageResult = bugReportQueryService.findAllBugReports(condition, page, size);

        Map<Long, MemberWithProfileImageResult> membersById = memberQueryService.findMemberWithProfileImagesByIds(
            pageResult.content().stream().map(dto -> dto.memberId().value()).toList()
        );

        PageResult<BugReportListItemResponse> responsePage = pageResult.map(
            dto -> toBugReportListItemResponse(dto, toMemberSummaryResponse(membersById.get(dto.memberId().value())))
        );
        return PaginationResponse.from(responsePage);
    }

    public BugReportDetailResponse getBugReport(Long id) {
        BugReportDetailResult detail = bugReportQueryService.findDetailById(BugReportId.of(id));

        MemberSummaryResponse member = memberQueryService.findMemberWithProfileImage(detail.memberId())
            .map(this::toMemberSummaryResponse)
            .orElse(null);

        List<FileResponse> images = toFileResponses(detail.imageFileIds());

        return toBugReportDetailResponse(detail, member, images);
    }

    public void changeStatus(Long id, String status, String answer) {
        BugReportStatusUpdateCommand command = BugReportStatusUpdateCommand.of(
            BugReportId.of(id), BugReportStatus.from(status), answer
        );
        bugReportCommandService.changeStatus(command);
    }

    public void classify(Long id, String category, String priority) {
        BugReportClassifyCommand command = BugReportClassifyCommand.of(
            BugReportId.of(id), BugReportCategory.from(category), BugReportPriority.from(priority)
        );
        bugReportCommandService.classify(command);
    }

    public void assign(Long id, Long assigneeAdminId) {
        BugReportAssignCommand command = BugReportAssignCommand.of(BugReportId.of(id), assigneeAdminId);
        bugReportCommandService.assign(command);
    }

    private MemberSummaryResponse toMemberSummaryResponse(MemberWithProfileImageResult result) {
        if (result == null) {
            return null;
        }
        return MemberSummaryResponse.from(result.id(), result.nickname());
    }

    private BugReportListItemResponse toBugReportListItemResponse(BugReportListItemResult dto, MemberSummaryResponse member) {
        return BugReportListItemResponse.from(
            dto.id(),
            member,
            dto.device(),
            dto.title(),
            dto.status() != null ? dto.status().name() : null,
            dto.category() != null ? dto.category().name() : null,
            dto.priority() != null ? dto.priority().name() : null,
            dto.imageCount(),
            dto.createdAt()
        );
    }

    private BugReportDetailResponse toBugReportDetailResponse(BugReportDetailResult dto, MemberSummaryResponse member, List<FileResponse> images) {
        return BugReportDetailResponse.from(
            dto.id().value(),
            member,
            dto.device(),
            dto.title(),
            dto.content(),
            dto.status() != null ? dto.status().name() : null,
            dto.category() != null ? dto.category().name() : null,
            dto.priority() != null ? dto.priority().name() : null,
            dto.assigneeAdminId(),
            dto.adminAnswer(),
            dto.resolvedAt(),
            dto.appVersion(),
            dto.platform() != null ? dto.platform().name() : null,
            dto.osVersion(),
            images,
            dto.createdAt(),
            dto.updatedAt()
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
