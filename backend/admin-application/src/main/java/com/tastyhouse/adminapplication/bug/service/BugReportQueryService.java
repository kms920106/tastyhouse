package com.tastyhouse.adminapplication.bug.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.bug.port.out.BugReportDetailResult;
import com.tastyhouse.application.bug.port.out.BugReportImageResult;
import com.tastyhouse.application.bug.port.out.BugReportListItemResult;
import com.tastyhouse.application.bug.port.out.BugReportQueryPort;
import com.tastyhouse.application.bug.port.out.BugReportSearchCondition;
import com.tastyhouse.application.member.port.out.MemberQueryPort;
import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapplication.bug.response.BugReportDetailResponse;
import com.tastyhouse.adminapplication.bug.response.BugReportListItemResponse;
import com.tastyhouse.adminapplication.bug.response.MemberSummaryResponse;
import com.tastyhouse.adminapplication.file.response.FileResponse;
import com.tastyhouse.adminapplication.bug.port.in.BugReportQueryUseCase;

/**
 * 버그 제보 관리 조회 서비스.
 *
 * <p>읽기 포트({@link BugReportQueryPort})만 주입해 제보를 조회하고 Response를 조립한다. write
 * 포트를 주입하지 않으며, 쓰기는 {@link BugReportCommandService}가 담당한다.
 *
 * <p>제보자 요약 정보는 다른 컨텍스트(member)의 조회 서비스에서 가져와 이 서비스가 합성한다. 첨부
 * 이미지는 {@link BugReportQueryPort}가 이미 파일명·URL까지 join으로 함께 가져오므로 이 서비스는 추가
 * 파일 조회 없이 그대로 매핑만 한다.
 *
 * <p>HTTP 경계에서 받은 {@code String} 필터값은 여기서 core enum으로 승격하고, Response로 내보낼 때는
 * 다시 {@code name()} 문자열로 되돌린다(api 모듈은 core enum을 노출하지 않는다).
 */
@Service
@Transactional(readOnly = true)
public class BugReportQueryService implements BugReportQueryUseCase {

    private final BugReportQueryPort bugReportQueryPort;
    private final MemberQueryPort memberQueryPort;

    public BugReportQueryService(BugReportQueryPort bugReportQueryPort, MemberQueryPort memberQueryPort) {
        this.bugReportQueryPort = bugReportQueryPort;
        this.memberQueryPort = memberQueryPort;
    }

    @Override
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
            memberId,
            status == null ? null : BugReportStatus.from(status),
            category == null ? null : BugReportCategory.from(category),
            priority == null ? null : BugReportPriority.from(priority)
        );
        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<BugReportListItemResult> pageResult = bugReportQueryPort.findBugReports(condition, pageQuery);

        Map<Long, MemberWithProfileImageResult> membersById = memberQueryPort.findMemberWithProfileImagesByIds(
            pageResult.content().stream().map(BugReportListItemResult::memberId).toList()
        );

        PageResult<BugReportListItemResponse> responsePage = pageResult.map(
            dto -> toBugReportListItemResponse(dto, toMemberSummaryResponse(membersById.get(dto.memberId())))
        );
        return PaginationResponse.from(responsePage);
    }

    @Override
    public BugReportDetailResponse getBugReport(Long id) {
        BugReportDetailResult detail = bugReportQueryPort.findDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BUG_REPORT_NOT_FOUND));

        MemberSummaryResponse member = memberQueryPort.findMemberWithProfileImageById(MemberId.of(detail.memberId()))
            .map(this::toMemberSummaryResponse)
            .orElse(null);

        List<FileResponse> images = toFileResponses(detail.images());

        return toBugReportDetailResponse(detail, member, images);
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
            dto.id(),
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

    /**
     * DAO가 join으로 함께 가져온 파일명·URL로 조립한다(추가 조회 없음).
     */
    private List<FileResponse> toFileResponses(List<BugReportImageResult> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream()
            .map(image -> FileResponse.of(image.fileId(), image.fileName(), image.imageUrl()))
            .toList();
    }
}
