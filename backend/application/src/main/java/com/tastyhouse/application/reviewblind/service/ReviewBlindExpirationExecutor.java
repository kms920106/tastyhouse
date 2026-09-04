package com.tastyhouse.application.reviewblind.service;

import com.tastyhouse.application.shared.marker.BatchApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.service.ReviewBlindRequestService;

/**
 * 게시중단 만료 재노출 <b>1건</b>의 트랜잭션 경계를 담당하는 얇은 빈.
 *
 * <p><b>별도 빈으로 분리한 이유(핵심)</b>: 건별 격리는 각 건이 독립 트랜잭션이어야 성립하는데, 같은 빈의
 * 메서드를 호출하면 Spring 프록시를 거치지 않아(self-invocation) {@code @Transactional}이 적용되지
 * 않는다. 그러면 한 건이 실패했을 때 롤백 경계가 없어 앞서 성공한 건들까지 함께 말려 들어간다.
 * {@code ReservationBookingExecutor}·{@code PaymentConfirmationExecutor}가 같은 이유로 분리된 선례다.
 *
 * <p>비즈니스 로직을 갖지 않는다 — 재노출 규칙 본체는 도메인 서비스
 * ({@link ReviewBlindRequestService#expire})가 소유한다.
 */
@Component
@BatchApp
public class ReviewBlindExpirationExecutor {

    private static final Logger log = LoggerFactory.getLogger(ReviewBlindExpirationExecutor.class);

    private final ReviewBlindRequestService reviewBlindRequestService;

    public ReviewBlindExpirationExecutor(ReviewBlindRequestService reviewBlindRequestService) {
        this.reviewBlindRequestService = reviewBlindRequestService;
    }

    /**
     * 한 건을 독립 트랜잭션에서 재노출한다.
     *
     * <p>실패해도 예외를 밖으로 던지지 않아 다음 건 처리가 이어진다 — 한 건의 실패가 전체 잡을 멈추지
     * 않게 하는 것이 이 배치의 요구사항이다.
     *
     * @return 성공 여부
     */
    @Transactional
    public boolean expire(ReviewBlindRequest request) {
        try {
            reviewBlindRequestService.expire(request.getId());
            return true;
        } catch (Exception e) {
            log.error("게시중단 만료 재노출 실패: blindRequestId={}, reviewId={}, blindUntil={}",
                request.getId(), request.getReviewId().value(), request.getBlindUntil(), e);
            return false;
        }
    }
}
