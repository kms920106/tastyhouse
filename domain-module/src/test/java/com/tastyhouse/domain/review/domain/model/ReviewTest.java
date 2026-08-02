package com.tastyhouse.domain.review.domain.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.review.model.Review;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.vo.ReviewId;

import static org.assertj.core.api.Assertions.assertThat;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class ReviewTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 숨김 처리되지 않은 상태다")
    void of_createsTransientReview() {
        Review review = Review.of(
            ShopId.of(1L), ProductId.of(2L), MemberId.of(3L), "맛있어요",
            4.5, 4.0, 5.0, 4.0, null, null, null,
            true, OrderId.of(10L)
        );

        assertThat(review.getId()).isNull();
        assertThat(review.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(review.getProductId()).isEqualTo(ProductId.of(2L));
        assertThat(review.getMemberId()).isEqualTo(MemberId.of(3L));
        assertThat(review.getContent()).isEqualTo("맛있어요");
        assertThat(review.getOrderId()).isEqualTo(OrderId.of(10L));
        assertThat(review.isWillRevisit()).isTrue();
        assertThat(review.isHidden()).isFalse();
        assertThat(review.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("hide/unhide는 숨김 플래그를 전환한다")
    void hideUnhide_togglesHidden() {
        Review review = Review.of(
            ShopId.of(1L), ProductId.of(2L), MemberId.of(3L), "맛있어요",
            4.5, 4.0, 5.0, 4.0, null, null, null,
            true, OrderId.of(10L)
        );

        review.hide();
        assertThat(review.isHidden()).isTrue();

        review.unhide();
        assertThat(review.isHidden()).isFalse();
    }

    @Test
    @DisplayName("updateContent는 내용·평점·재방문 여부를 변경한다")
    void updateContent_changesFields() {
        Review review = Review.of(
            ShopId.of(1L), ProductId.of(2L), MemberId.of(3L), "맛있어요",
            4.5, 4.0, 5.0, 4.0, null, null, null,
            true, OrderId.of(10L)
        );

        review.updateContent(
            "별로예요", 2.0, 2.0, 2.0, 2.0, 3.0, 3.0, 3.0, false
        );

        assertThat(review.getContent()).isEqualTo("별로예요");
        assertThat(review.getTotalRating()).isEqualTo(2.0);
        assertThat(review.getTasteRating()).isEqualTo(2.0);
        assertThat(review.getAtmosphereRating()).isEqualTo(3.0);
        assertThat(review.isWillRevisit()).isFalse();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각·숨김 상태를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);

        Review review = Review.reconstitute(
            1L, ShopId.of(2L), ProductId.of(3L), MemberId.of(4L), "맛있어요",
            4.5, 4.0, 5.0, 4.0, null, null, null,
            true, OrderId.of(10L), true, createdAt
        );

        assertThat(review.getId()).isEqualTo(1L);
        assertThat(review.getReviewId()).isEqualTo(ReviewId.of(1L));
        assertThat(review.isHidden()).isTrue();
        assertThat(review.getCreatedAt()).isEqualTo(createdAt);
    }
}
