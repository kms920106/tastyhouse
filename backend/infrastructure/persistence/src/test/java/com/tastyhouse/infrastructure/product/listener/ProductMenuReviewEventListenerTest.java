package com.tastyhouse.infrastructure.product.listener;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.event.MenuReviewCreatedEvent;
import com.tastyhouse.domain.menureview.event.MenuReviewDeletedEvent;
import com.tastyhouse.domain.menureview.event.MenuReviewRatingChangedEvent;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.product.service.ProductReviewStatsService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * {@link ProductMenuReviewEventListener}의 동작을 봉인하는 순수 단위 테스트.
 *
 * <p>이 리스너는 상품 평점·평가 수라는 <b>영속 상태</b>를 갱신하므로, 기록이 아니라 "어떤 상품 id로 통계
 * 갱신을 호출하는가"를 검증한다. 이벤트 3종 모두가 같은 재집계를 트리거해야 한다 — 하나라도 빠지면
 * {@code PRODUCT.rating}이 조용히 낡는다.
 *
 * <p>{@code productId == null} 가드도 함께 검증한다 — 그 예외는 AFTER_COMMIT 리스너에서 조용히 유실된다.
 *
 * <p>{@code @Async}·{@code REQUIRES_NEW} 배선은 프레임워크 몫이라 검증 대상이 아니다.
 */
class ProductMenuReviewEventListenerTest {

    private final ProductReviewStatsService productReviewStatsService = mock(ProductReviewStatsService.class);

    private final ProductMenuReviewEventListener listener =
        new ProductMenuReviewEventListener(productReviewStatsService);

    @Test
    @DisplayName("메뉴 평가 등록 이벤트를 받으면 해당 상품의 통계를 갱신한다")
    void updatesStatsOnMenuReviewCreated() {
        listener.onMenuReviewCreated(new MenuReviewCreatedEvent(
            MenuReviewId.of(801L), MemberId.of(802L), ShopId.of(803L), ProductId.of(701L), occurredAt()
        ));

        verify(productReviewStatsService).updateReviewStats(701L);
    }

    @Test
    @DisplayName("메뉴 평가 평점 변경 이벤트를 받으면 해당 상품의 통계를 갱신한다")
    void updatesStatsOnMenuReviewRatingChanged() {
        listener.onMenuReviewRatingChanged(new MenuReviewRatingChangedEvent(
            MenuReviewId.of(804L), MemberId.of(805L), ShopId.of(806L), ProductId.of(702L), occurredAt()
        ));

        verify(productReviewStatsService).updateReviewStats(702L);
    }

    @Test
    @DisplayName("메뉴 평가 삭제 이벤트를 받으면 해당 상품의 통계를 갱신한다")
    void updatesStatsOnMenuReviewDeleted() {
        listener.onMenuReviewDeleted(new MenuReviewDeletedEvent(
            MenuReviewId.of(807L), MemberId.of(808L), ShopId.of(809L), ProductId.of(703L), occurredAt()
        ));

        verify(productReviewStatsService).updateReviewStats(703L);
    }

    @Test
    @DisplayName("상품이 없는 이벤트면 통계를 갱신하지 않는다(가드)")
    void skipsWhenEventHasNoProduct() {
        listener.onMenuReviewCreated(new MenuReviewCreatedEvent(
            MenuReviewId.of(810L), MemberId.of(811L), ShopId.of(812L), null, occurredAt()
        ));
        listener.onMenuReviewRatingChanged(new MenuReviewRatingChangedEvent(
            MenuReviewId.of(813L), MemberId.of(814L), ShopId.of(815L), null, occurredAt()
        ));
        listener.onMenuReviewDeleted(new MenuReviewDeletedEvent(
            MenuReviewId.of(816L), MemberId.of(817L), ShopId.of(818L), null, occurredAt()
        ));

        verify(productReviewStatsService, never()).updateReviewStats(anyLong());
    }

    private LocalDateTime occurredAt() {
        return LocalDateTime.of(2026, 4, 15, 15, 0);
    }
}
