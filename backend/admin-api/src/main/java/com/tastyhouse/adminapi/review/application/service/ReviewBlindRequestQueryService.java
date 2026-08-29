package com.tastyhouse.adminapi.review.application.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.review.query.ReviewBlindRequestDetailResult;
import com.tastyhouse.infrastructure.review.query.ReviewBlindRequestListItemResult;
import com.tastyhouse.infrastructure.review.query.ReviewBlindRequestQueryDao;
import com.tastyhouse.infrastructure.review.query.ReviewBlindRequestSearchCondition;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.review.adapter.in.web.response.ReviewBlindRequestDetailResponse;
import com.tastyhouse.adminapi.review.adapter.in.web.response.ReviewBlindRequestListItemResponse;
import com.tastyhouse.adminapi.review.application.port.in.ReviewBlindRequestQueryUseCase;

/**
 * 리뷰 게시중단 요청 심사 조회 서비스(admin, CQRS query 측).
 *
 * <p>{@code status}/{@code reason}은 HTTP 경계에서 문자열로 받아 여기서 도메인 enum으로 승격한다 —
 * Request record는 domain-free 원칙에 따라 enum을 직접 다루지 않는다.
 *
 * <p>명령 동작은 {@link ReviewBlindRequestCommandService}로 분리했다(CQRS).
 */
@Service
@Transactional(readOnly = true)
public class ReviewBlindRequestQueryService implements ReviewBlindRequestQueryUseCase {

    private final ReviewBlindRequestQueryDao reviewBlindRequestQueryDao;

    public ReviewBlindRequestQueryService(ReviewBlindRequestQueryDao reviewBlindRequestQueryDao) {
        this.reviewBlindRequestQueryDao = reviewBlindRequestQueryDao;
    }

    /**
     * 게시중단 요청 목록 — 상점·상태·사유·기간으로 필터링한다.
     */
    @Override
    public PaginationResponse<ReviewBlindRequestListItemResponse> getBlindRequests(
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
        PageResult<ReviewBlindRequestListItemResponse> pageResult = reviewBlindRequestQueryDao
            .findBlindRequestPage(condition, PageQuery.of(page, size))
            .map(this::toReviewBlindRequestListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    /**
     * 게시중단 요청 심사 상세.
     */
    @Override
    public ReviewBlindRequestDetailResponse getBlindRequest(Long id) {
        ReviewBlindRequestDetailResult detail = reviewBlindRequestQueryDao.findBlindRequestDetail(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_FOUND));
        return toReviewBlindRequestDetailResponse(detail);
    }

    private ReviewBlindRequestListItemResponse toReviewBlindRequestListItemResponse(ReviewBlindRequestListItemResult dto) {
        return ReviewBlindRequestListItemResponse.from(
            dto.id(),
            dto.reviewId(),
            dto.shopId(),
            dto.shopName(),
            dto.reason().name(),
            dto.reason().getDescription(),
            dto.status().name(),
            dto.status().getDescription(),
            dto.reviewContent(),
            dto.reviewTotalRating(),
            dto.blindUntil(),
            dto.createdAt()
        );
    }

    private ReviewBlindRequestDetailResponse toReviewBlindRequestDetailResponse(ReviewBlindRequestDetailResult dto) {
        return ReviewBlindRequestDetailResponse.from(
            dto.id(),
            dto.reviewId(),
            dto.shopId(),
            dto.shopName(),
            dto.reason().name(),
            dto.reason().getDescription(),
            dto.status().name(),
            dto.status().getDescription(),
            dto.reviewContent(),
            dto.reviewTotalRating(),
            dto.detailReason(),
            dto.rejectReason(),
            dto.blindUntil(),
            dto.reviewImageUrls(),
            dto.attachmentUrls(),
            dto.reviewMemberNickname(),
            dto.reviewHidden(),
            dto.reviewCreatedAt(),
            dto.createdAt()
        );
    }
}
