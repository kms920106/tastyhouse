package com.tastyhouse.application.product.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductPriceStateMapperTest {

    @Test
    @DisplayName("ProductPrice → ProductPriceState → ProductPrice 왕복 시 모든 필드가 보존된다")
    void productPriceRoundTrip() {
        ProductPrice original = ProductPrice.reconstitute(
            131L,
            ProductId.of(132L),
            "곱빼기",
            12000,
            11000,
            10500,
            2,
            LocalDateTime.of(2026, 2, 16, 10, 16),
            LocalDateTime.of(2026, 2, 17, 10, 17),
            LocalDateTime.of(2026, 2, 18, 10, 18)
        );

        ProductPrice restored = ProductPriceStateMapper.toDomain(ProductPriceStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
