package com.tastyhouse.application.product.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCommonOptionStateMapperTest {

    @Test
    @DisplayName("ProductCommonOption → ProductCommonOptionState → ProductCommonOption 왕복 시 모든 필드가 보존된다")
    void productCommonOptionRoundTrip() {
        ProductCommonOption original = ProductCommonOption.reconstitute(
            151L,
            ProductOptionGroupId.of(152L),
            "시럽",
            700,
            7,
            true,
            LocalDateTime.of(2026, 2, 20, 10, 20),
            false
        );

        ProductCommonOption restored = ProductCommonOptionStateMapper.toDomain(ProductCommonOptionStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
