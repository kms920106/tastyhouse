package com.tastyhouse.application.product.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductOptionStateMapperTest {

    @Test
    @DisplayName("ProductOption → ProductOptionState → ProductOption 왕복 시 모든 필드가 보존된다")
    void productOptionRoundTrip() {
        ProductOption original = ProductOption.reconstitute(
            141L,
            ProductOptionGroupId.of(142L),
            "샷 추가",
            500,
            6,
            true,
            LocalDateTime.of(2026, 2, 19, 10, 19),
            false,
            2,
            300
        );

        ProductOption restored = ProductOptionStateMapper.toDomain(ProductOptionStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
