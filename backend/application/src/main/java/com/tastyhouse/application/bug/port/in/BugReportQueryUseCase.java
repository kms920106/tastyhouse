package com.tastyhouse.application.bug.port.in;

import com.tastyhouse.application.bug.port.out.BugReportDetailWithMemberResult;
import com.tastyhouse.application.bug.port.out.BugReportListItemWithMemberResult;
import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 버그 신고 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code BugReportQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p><b>챕터 06</b> — 반환 타입은 Swagger를 아는 {@code *Response}가 아니라 프레임워크-프리
 * {@code *Result}다. Response 조립과 {@code PaginationResponse} 매핑은 컨트롤러가 담당한다.
 * 제보와 제보자는 서로 다른 읽기 포트에서 오므로, 두 결과의 합성만 유스케이스가 맡고 그 산출물도
 * 결과 타입({@code *WithMemberResult})으로 내보낸다.
 */
@AdminApp
public interface BugReportQueryUseCase {

    PageResult<BugReportListItemWithMemberResult> getBugReports(
        String title,
        String content,
        Long memberId,
        String status,
        String category,
        String priority,
        int page,
        int size
    );

    BugReportDetailWithMemberResult getBugReport(Long id);
}
