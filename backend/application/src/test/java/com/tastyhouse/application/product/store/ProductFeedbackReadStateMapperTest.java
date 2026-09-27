package com.tastyhouse.application.product.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductFeedbackRead;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductFeedbackReadStateMapperTest {

    @Test
    @DisplayName("ProductFeedbackRead → ProductFeedbackReadState → ProductFeedbackRead 왕복 시 모든 필드가 보존된다")
    void productFeedbackReadRoundTrip() {
        ProductFeedbackRead original = ProductFeedbackRead.reconstitute(
            51L,
            ShopId.of(52L),
            LocalDateTime.of(2026, 2, 5, 10, 5),
            LocalDateTime.of(2026, 2, 6, 10, 6),
            LocalDateTime.of(2026, 2, 7, 10, 7)
        );

        ProductFeedbackRead restored = ProductFeedbackReadStateMapper.toDomain(ProductFeedbackReadStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
