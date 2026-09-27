package com.tastyhouse.application.product.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.model.ProductFeedback;
import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductFeedbackStateMapperTest {

    @Test
    @DisplayName("ProductFeedback → ProductFeedbackState → ProductFeedback 왕복 시 모든 필드가 보존된다")
    void productFeedbackRoundTrip() {
        ProductFeedback original = ProductFeedback.reconstitute(
            61L,
            ProductId.of(62L),
            ShopId.of(63L),
            MemberId.of(64L),
            ProductFeedbackType.SOLD_OUT,
            "품절이 잦아요",
            LocalDateTime.of(2026, 2, 8, 10, 8),
            LocalDateTime.of(2026, 2, 9, 10, 9)
        );

        ProductFeedback restored = ProductFeedbackStateMapper.toDomain(ProductFeedbackStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
