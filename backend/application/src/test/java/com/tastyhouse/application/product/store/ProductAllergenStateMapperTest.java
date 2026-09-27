package com.tastyhouse.application.product.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.AllergenType;
import com.tastyhouse.domain.product.model.ProductAllergen;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductAllergenStateMapperTest {

    @Test
    @DisplayName("ProductAllergen → ProductAllergenState → ProductAllergen 왕복 시 모든 필드가 보존된다")
    void productAllergenRoundTrip() {
        ProductAllergen original = ProductAllergen.reconstitute(
            11L,
            ProductId.of(12L),
            AllergenType.PEANUT,
            LocalDateTime.of(2026, 2, 1, 10, 1),
            LocalDateTime.of(2026, 2, 2, 10, 2)
        );

        ProductAllergen restored = ProductAllergenStateMapper.toDomain(ProductAllergenStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
