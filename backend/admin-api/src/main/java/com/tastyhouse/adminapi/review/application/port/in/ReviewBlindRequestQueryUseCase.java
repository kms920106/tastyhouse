package com.tastyhouse.adminapi.review.application.port.in;

import java.time.LocalDate;

import com.tastyhouse.adminapi.review.adapter.in.web.response.ReviewBlindRequestDetailResponse;
import com.tastyhouse.adminapi.review.adapter.in.web.response.ReviewBlindRequestListItemResponse;
import com.tastyhouse.apicommon.common.PaginationResponse;

/**
 * 리뷰 블라인드 요청 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ReviewBlindRequestQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ReviewBlindRequestQueryUseCase {

    PaginationResponse<ReviewBlindRequestListItemResponse> getBlindRequests(
        Long shopId,
        String status,
        String reason,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    );

    ReviewBlindRequestDetailResponse getBlindRequest(Long id);
}
