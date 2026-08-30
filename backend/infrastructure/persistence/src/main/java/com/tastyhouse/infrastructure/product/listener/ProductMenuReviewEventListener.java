package com.tastyhouse.infrastructure.product.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.domain.menureview.event.MenuReviewCreatedEvent;
import com.tastyhouse.domain.menureview.event.MenuReviewDeletedEvent;
import com.tastyhouse.domain.menureview.event.MenuReviewRatingChangedEvent;
import com.tastyhouse.domain.product.service.ProductReviewStatsService;

/**
 * 메뉴 평가 등록·수정·삭제 이벤트를 받아 상품의 평점·평가 수를 갱신하는 크로스커팅 리스너(분류 E).
 *
 * <p>infrastructure-module에 두는 이유(기존 {@code ProductReviewEventListener}의 소유 근거 그대로):
 * 평가는 web-api에서 등록되지만 admin-api의 숨김·삭제로도 통계가 바뀐다. 특정 api 모듈에 리스너를 두면
 * 다른 모듈이 이벤트를 발행할 때 갱신이 누락되므로, 모든 실행 모듈이 공통으로 의존하는
 * infrastructure-module이 소유한다.
 *
 * <p><b>구독 대상이 REVIEW 이벤트에서 MENU_REVIEW 이벤트로 이관됐다.</b> {@code PRODUCT.rating}의 근거가
 * MENU_REVIEW로 완전히 옮겨갔으므로 구독도 하나만 남는 것이 맞다 — 두 리스너가 같은
 * {@link ProductReviewStatsService}를 호출하면 재집계가 두 번 돌고 "어느 쪽이 진짜 근거인가"가 코드에서
 * 사라진다. {@code ReviewCreatedEvent}/{@code ReviewDeletedEvent}는 발행만 남고 소비자가 0이다
 * (사유는 각 이벤트 Javadoc 참고).
 *
 * <p>갱신 규칙 자체는 도메인 서비스 {@link ProductReviewStatsService}가 갖고, 이 리스너는 이벤트 수신과
 * 트랜잭션 경계(커밋 후 새 트랜잭션)만 담당한다.
 *
 * <p>{@code productId == null} 가드는 유지한다 — MENU_REVIEW의 {@code product_id}는 NOT NULL이지만,
 * 이벤트 record가 VO를 담고 있어 향후 발행 경로가 늘 때 null이 실릴 수 있고 그 예외는 AFTER_COMMIT
 * 리스너에서 조용히 유실된다.
 */
@Component
public class ProductMenuReviewEventListener {

    private final ProductReviewStatsService productReviewStatsService;

    public ProductMenuReviewEventListener(ProductReviewStatsService productReviewStatsService) {
        this.productReviewStatsService = productReviewStatsService;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMenuReviewCreated(MenuReviewCreatedEvent event) {
        if (event.productId() == null) {
            return;
        }
        productReviewStatsService.updateReviewStats(event.productId().value());
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMenuReviewRatingChanged(MenuReviewRatingChangedEvent event) {
        if (event.productId() == null) {
            return;
        }
        productReviewStatsService.updateReviewStats(event.productId().value());
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMenuReviewDeleted(MenuReviewDeletedEvent event) {
        if (event.productId() == null) {
            return;
        }
        productReviewStatsService.updateReviewStats(event.productId().value());
    }
}
