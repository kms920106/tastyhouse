package com.tastyhouse.adminapplication.bug.port.in;

import com.tastyhouse.adminapplication.bug.response.BugReportDetailResponse;
import com.tastyhouse.adminapplication.bug.response.BugReportListItemResponse;
import com.tastyhouse.apicommon.common.PaginationResponse;

/**
 * 버그 신고 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code BugReportQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface BugReportQueryUseCase {

    PaginationResponse<BugReportListItemResponse> getBugReports(
        String title,
        String content,
        Long memberId,
        String status,
        String category,
        String priority,
        int page,
        int size
    );

    BugReportDetailResponse getBugReport(Long id);
}
