package com.tastyhouse.application.product.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductNutrition;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductNutritionStateMapperTest {

    @Test
    @DisplayName("ProductNutrition → ProductNutritionState → ProductNutrition 왕복 시 모든 필드가 보존된다")
    void productNutritionRoundTrip() {
        ProductNutrition original = ProductNutrition.reconstitute(
            41L,
            ProductId.of(42L),
            "1인분",
            "300g",
            "매운맛",
            "대",
            500,
            20,
            30,
            5,
            800,
            60,
            70,
            15,
            1,
            90,
            true,
            LocalDateTime.of(2026, 2, 3, 10, 3),
            LocalDateTime.of(2026, 2, 4, 10, 4)
        );

        ProductNutrition restored = ProductNutritionStateMapper.toDomain(ProductNutritionStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
