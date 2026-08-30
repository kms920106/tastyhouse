package com.tastyhouse.adminapplication.review.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.adminapplication.review.port.in.ReviewBlindRequestApproveCommand;
import com.tastyhouse.adminapplication.review.port.in.ReviewBlindRequestCommandUseCase;
import com.tastyhouse.adminapplication.review.port.in.ReviewBlindRequestRejectCommand;
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
public class ReviewBlindRequestCommandService implements ReviewBlindRequestCommandUseCase {

    private final ReviewBlindRequestService reviewBlindRequestService;

    public ReviewBlindRequestCommandService(ReviewBlindRequestService reviewBlindRequestService) {
        this.reviewBlindRequestService = reviewBlindRequestService;
    }

    /**
     * 게시중단 요청을 승인하고, 대상 리뷰를 즉시 숨긴다.
     *
     * <p>재노출 기한(승인 시각 + 30일) 계산의 기준 시각을 여기서 해석해 도메인에 넘긴다 — 도메인은
     * 프레임워크-프리라 시계를 주입받을 수 없고, 직접 {@code now()}를 부르면 단위 테스트에서 만료를
     * 고정할 수 없기 때문이다.
     */
    @Override
    public void approveBlindRequest(ReviewBlindRequestApproveCommand command) {
        Long id = command.requestId();
        reviewBlindRequestService.approve(id, LocalDateTime.now());
    }

    /**
     * 게시중단 요청을 반려한다. 리뷰는 노출 상태를 유지한다.
     */
    @Override
    public void rejectBlindRequest(ReviewBlindRequestRejectCommand command) {
        Long id = command.requestId();
        String rejectReason = command.rejectReason();
        reviewBlindRequestService.reject(id, rejectReason);
    }
}
