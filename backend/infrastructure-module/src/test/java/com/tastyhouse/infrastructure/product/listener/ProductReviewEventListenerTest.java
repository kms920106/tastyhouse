package com.tastyhouse.infrastructure.product.listener;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.service.ProductReviewStatsService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.event.ReviewCreatedEvent;
import com.tastyhouse.domain.review.event.ReviewDeletedEvent;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * {@link ProductReviewEventListener}의 현재 동작을 봉인하는 순수 단위 테스트.
 *
 * <p>이 리스너는 상품 평점·리뷰 수라는 <b>영속 상태</b>를 갱신하므로, 기록이 아니라 "어떤 상품 id로
 * 통계 갱신을 호출하는가"를 검증한다. 특히 {@code productId == null} 가드가 핵심이다 — 가게 단위 리뷰는
 * 상품이 없어 이 필드가 null인데, 가드가 빠지면 {@code ProductId.of(null)}이 던지는 예외가
 * AFTER_COMMIT 리스너에서 조용히 유실된다.
 *
 * <p>{@code @Async}·{@code REQUIRES_NEW} 배선은 프레임워크 몫이라 검증 대상이 아니다.
 */
class ProductReviewEventListenerTest {

    private final ProductReviewStatsService productReviewStatsService = mock(ProductReviewStatsService.class);

    private final ProductReviewEventListener listener = new ProductReviewEventListener(productReviewStatsService);

    @Test
    @DisplayName("리뷰 등록 이벤트를 받으면 해당 상품의 리뷰 통계를 갱신한다")
    void updatesStatsOnReviewCreated() {
        listener.onReviewCreated(createdEvent(ProductId.of(701L)));

        verify(productReviewStatsService).updateReviewStats(701L);
    }

    @Test
    @DisplayName("리뷰 삭제 이벤트를 받으면 해당 상품의 리뷰 통계를 갱신한다")
    void updatesStatsOnReviewDeleted() {
        listener.onReviewDeleted(deletedEvent(ProductId.of(702L)));

        verify(productReviewStatsService).updateReviewStats(702L);
    }

    @Test
    @DisplayName("상품이 없는 리뷰(가게 단위) 등록이면 통계를 갱신하지 않는다")
    void skipsWhenCreatedEventHasNoProduct() {
        listener.onReviewCreated(createdEvent(null));

        verify(productReviewStatsService, never()).updateReviewStats(anyLong());
    }

    @Test
    @DisplayName("상품이 없는 리뷰(가게 단위) 삭제면 통계를 갱신하지 않는다")
    void skipsWhenDeletedEventHasNoProduct() {
        listener.onReviewDeleted(deletedEvent(null));

        verify(productReviewStatsService, never()).updateReviewStats(anyLong());
    }

    private ReviewCreatedEvent createdEvent(ProductId productId) {
        return new ReviewCreatedEvent(
            ReviewId.of(703L),
            MemberId.of(704L),
            ShopId.of(705L),
            productId,
            LocalDateTime.of(2026, 4, 15, 15, 0)
        );
    }

    private ReviewDeletedEvent deletedEvent(ProductId productId) {
        return new ReviewDeletedEvent(
            ReviewId.of(706L),
            MemberId.of(707L),
            productId,
            LocalDateTime.of(2026, 4, 16, 16, 30)
        );
    }
}
