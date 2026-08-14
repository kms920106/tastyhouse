package com.tastyhouse.adminapi.review;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.service.ReviewBlindRequestService;

/**
 * 리뷰 게시중단 요청 심사 변경 서비스(admin, CQRS command 측).
 *
 * <p>승인 시 요청 상태 전이와 대상 리뷰 숨김 반영이 한 트랜잭션에서 함께 일어나야 하는 원자 연산은
 * 도메인 서비스 {@link ReviewBlindRequestService}가 담당한다(요청자 ceo·심사자 admin 공유 규칙).
 *
 * <p>조회 전용 동작은 {@link ReviewBlindRequestQueryService}로 분리했다(CQRS).
 */
@Service
@Transactional
public class ReviewBlindRequestCommandService {

    private final ReviewBlindRequestService reviewBlindRequestService;

    public ReviewBlindRequestCommandService(ReviewBlindRequestService reviewBlindRequestService) {
        this.reviewBlindRequestService = reviewBlindRequestService;
    }

    /**
     * 게시중단 요청을 승인하고, 대상 리뷰를 즉시 숨긴다.
     */
    public void approveBlindRequest(Long id) {
        reviewBlindRequestService.approve(id);
    }

    /**
     * 게시중단 요청을 반려한다. 리뷰는 노출 상태를 유지한다.
     */
    public void rejectBlindRequest(Long id, String rejectReason) {
        reviewBlindRequestService.reject(id, rejectReason);
    }
}
