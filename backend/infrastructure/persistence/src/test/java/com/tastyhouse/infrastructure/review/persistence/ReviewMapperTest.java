package com.tastyhouse.infrastructure.review.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewMapperTest {

    @Test
    @DisplayName("Review → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void toEntity() {
        ReviewJpaEntity entity = ReviewMapper.toEntity(review());

        assertThat(entity.getShopId()).isEqualTo(102L);
        assertThat(entity.getProductId()).isEqualTo(103L);
        assertThat(entity.getMemberId()).isEqualTo(104L);
        assertThat(entity.getContent()).isEqualTo("리뷰 본문");
        assertThat(entity.getTotalRating()).isEqualTo(4.5);
        assertThat(entity.getTasteRating()).isEqualTo(4.1);
        assertThat(entity.getAmountRating()).isEqualTo(3.2);
        assertThat(entity.getPriceRating()).isEqualTo(2.3);
        assertThat(entity.getAtmosphereRating()).isEqualTo(1.4);
        assertThat(entity.getKindnessRating()).isEqualTo(3.5);
        assertThat(entity.getHygieneRating()).isEqualTo(2.6);
        assertThat(entity.isWillRevisit()).isTrue();
        assertThat(entity.getOrderId()).isEqualTo(105L);
        assertThat(entity.isHidden()).isFalse();
        assertThat(entity.isOwnerOnly()).isTrue();
        assertThat(entity.getDeliveryRating()).isEqualTo(3);
        assertThat(entity.getDeliveryComment()).isEqualTo("배달 코멘트");
    }

    @Test
    @DisplayName("엔티티 → Review 변환 시 모든 필드가 보존된다")
    void toDomain() {
        Review original = review();
        ReviewJpaEntity entity = ReviewMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", original.getId());
        ReflectionTestUtils.setField(entity, "createdAt", original.getCreatedAt());
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 7, 2, 9, 0));

        Review restored = ReviewMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static Review review() {
        return Review.reconstitute(
            101L, ShopId.of(102L), ProductId.of(103L), MemberId.of(104L), "리뷰 본문",
            4.5, 4.1, 3.2, 2.3, 1.4, 3.5, 2.6, true, OrderId.of(105L), false, true,
            3, "배달 코멘트", LocalDateTime.of(2026, 7, 1, 9, 0));
    }
}
