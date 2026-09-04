package com.tastyhouse.adminapplication.bug.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.bug.port.out.BugReportDetailResult;
import com.tastyhouse.application.bug.port.out.BugReportDetailWithMemberResult;
import com.tastyhouse.application.bug.port.out.BugReportListItemResult;
import com.tastyhouse.application.bug.port.out.BugReportListItemWithMemberResult;
import com.tastyhouse.application.bug.port.out.BugReportQueryPort;
import com.tastyhouse.application.bug.port.out.BugReportSearchCondition;
import com.tastyhouse.application.member.port.out.MemberManagementQueryPort;
import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.adminapplication.bug.port.in.BugReportQueryUseCase;

/**
 * 버그 제보 관리 조회 서비스.
 *
 * <p>읽기 포트({@link BugReportQueryPort})만 주입해 제보를 조회한다. write 포트를 주입하지 않으며,
 * 쓰기는 {@link BugReportManagementCommandService}가 담당한다.
 *
 * <p>제보자 요약 정보는 다른 컨텍스트(member)의 읽기 포트에서 가져와 이 서비스가 합성한다. 첨부
 * 이미지는 {@link BugReportQueryPort}가 이미 파일명·URL까지 join으로 함께 가져오므로 이 서비스는 추가
 * 파일 조회를 하지 않는다.
 *
 * <p>HTTP 경계에서 받은 {@code String} 필터값은 여기서 core enum으로 승격한다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로(또는 제보자와 합성한
 * {@code *WithMemberResult}로) 반환하고 Response로 변환하지 않는다. 표현 계약(@Schema 붙은
 * Response·{@code FileResponse}·PaginationResponse) 조립과 enum → {@code name()} 문자열 변환은
 * 컨트롤러의 책임이다.
 */
@Service
@Transactional(readOnly = true)
public class BugReportQueryService implements BugReportQueryUseCase {

    private final BugReportQueryPort bugReportQueryPort;
    private final MemberManagementQueryPort memberManagementQueryPort;

    public BugReportQueryService(BugReportQueryPort bugReportQueryPort, MemberManagementQueryPort memberManagementQueryPort) {
        this.bugReportQueryPort = bugReportQueryPort;
        this.memberManagementQueryPort = memberManagementQueryPort;
    }

    @Override
    public PageResult<BugReportListItemWithMemberResult> getBugReports(
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

        Map<Long, MemberWithProfileImageResult> membersById = memberManagementQueryPort.findMemberWithProfileImagesByIds(
            pageResult.content().stream().map(BugReportListItemResult::memberId).toList()
        );

        return pageResult.map(
            dto -> new BugReportListItemWithMemberResult(dto, membersById.get(dto.memberId()))
        );
    }

    @Override
    public BugReportDetailWithMemberResult getBugReport(Long id) {
        BugReportDetailResult detail = bugReportQueryPort.findDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BUG_REPORT_NOT_FOUND));

        MemberWithProfileImageResult member = memberManagementQueryPort.findMemberWithProfileImageById(MemberId.of(detail.memberId()))
            .orElse(null);

        return new BugReportDetailWithMemberResult(detail, member);
    }
}
