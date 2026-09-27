package com.tastyhouse.application.menureview.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.model.MenuReview;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class MenuReviewStateMapperTest {

    @Test
    @DisplayName("MenuReview → MenuReviewState → MenuReview 왕복 시 모든 필드가 보존된다")
    void roundTrip() {
        MenuReview original = MenuReview.reconstitute(
            91L, MemberId.of(92L), ShopId.of(93L), ProductId.of(94L), OrderId.of(95L), OrderProductId.of(96L),
            4, "맛있어요", true,
            LocalDateTime.of(2026, 6, 1, 10, 0),
            LocalDateTime.of(2026, 6, 2, 11, 0));

        MenuReview restored = MenuReviewStateMapper.toDomain(MenuReviewStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
