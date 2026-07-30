package com.tastyhouse.infrastructure.product.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.core.domain.product.domain.service.ProductReviewStatsService;
import com.tastyhouse.core.domain.review.domain.event.ReviewCreatedEvent;
import com.tastyhouse.core.domain.review.domain.event.ReviewDeletedEvent;

/**
 * 리뷰 등록·삭제 이벤트를 받아 상품의 평점·리뷰 수를 갱신하는 크로스커팅 리스너(분류 E).
 *
 * <p>infrastructure-module에 두는 이유: 리뷰는 web-api에서 등록되지만 admin-api의 숨김·삭제로도
 * 통계가 바뀐다. 특정 api 모듈에 리스너를 두면 다른 모듈이 이벤트를 발행할 때 갱신이 누락되므로,
 * 모든 실행 모듈이 공통으로 의존하는 infrastructure-module이 소유한다.
 *
 * <p>갱신 규칙 자체는 도메인 서비스 {@link ProductReviewStatsService}가 갖고, 이 리스너는 이벤트 수신과
 * 트랜잭션 경계(커밋 후 새 트랜잭션)만 담당한다.
 */
@Component
@RequiredArgsConstructor
public class ProductReviewEventListener {

    private final ProductReviewStatsService productReviewStatsService;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReviewCreated(ReviewCreatedEvent event) {
        if (event.productId() == null) {
            return;
        }
        productReviewStatsService.updateReviewStats(event.productId());
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReviewDeleted(ReviewDeletedEvent event) {
        if (event.productId() == null) {
            return;
        }
        productReviewStatsService.updateReviewStats(event.productId());
    }
}
