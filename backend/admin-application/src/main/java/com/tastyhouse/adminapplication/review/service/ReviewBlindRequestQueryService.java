package com.tastyhouse.adminapplication.review.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestDetailResult;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestListItemResult;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestManagementQueryPort;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestSearchCondition;
import com.tastyhouse.adminapplication.review.port.in.ReviewBlindRequestQueryUseCase;

/**
 * 리뷰 게시중단 요청 심사 조회 서비스(admin, CQRS query 측).
 *
 * <p>{@code status}/{@code reason}은 HTTP 경계에서 문자열로 받아 여기서 도메인 enum으로 승격한다 —
 * Request record는 domain-free 원칙에 따라 enum을 직접 다루지 않는다.
 *
 * <p>명령 동작은 {@link ReviewBlindRequestCommandService}로 분리했다(CQRS).
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response·PaginationResponse) 조립은 컨트롤러의 책임이다.
 */
@Service
@Transactional(readOnly = true)
public class ReviewBlindRequestQueryService implements ReviewBlindRequestQueryUseCase {

    private final ReviewBlindRequestManagementQueryPort reviewBlindRequestManagementQueryPort;

    public ReviewBlindRequestQueryService(ReviewBlindRequestManagementQueryPort reviewBlindRequestManagementQueryPort) {
        this.reviewBlindRequestManagementQueryPort = reviewBlindRequestManagementQueryPort;
    }

    /**
     * 게시중단 요청 목록 — 상점·상태·사유·기간으로 필터링한다.
     */
    @Override
    public PageResult<ReviewBlindRequestListItemResult> getBlindRequests(
        Long shopId,
        String status,
        String reason,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    ) {
        ReviewBlindStatus blindStatus = status == null ? null : ReviewBlindStatus.from(status);
        ReviewBlindReason blindReason = reason == null ? null : ReviewBlindReason.from(reason);

        ReviewBlindRequestSearchCondition condition = ReviewBlindRequestSearchCondition.of(
            shopId, blindStatus, blindReason, startDate, endDate
        );
        return reviewBlindRequestManagementQueryPort.findBlindRequestPage(condition, PageQuery.of(page, size));
    }

    /**
     * 게시중단 요청 심사 상세.
     */
    @Override
    public ReviewBlindRequestDetailResult getBlindRequest(Long id) {
        return reviewBlindRequestManagementQueryPort.findBlindRequestDetail(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_FOUND));
    }
}
