package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.product.model.ProductNutrition;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductNutritionMapperTest {

    @Test
    @DisplayName("ProductNutrition → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductNutrition productNutrition = ProductNutrition.reconstitute(
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

        ProductNutritionJpaEntity entity = ProductNutritionMapper.toEntity(productNutrition);

        assertThat(entity.getProductId()).isEqualTo(42L);
        assertThat(entity.getServingSize()).isEqualTo("1인분");
        assertThat(entity.getTotalAmount()).isEqualTo("300g");
        assertThat(entity.getFlavor()).isEqualTo("매운맛");
        assertThat(entity.getSize()).isEqualTo("대");
        assertThat(entity.getCalorie()).isEqualTo(500);
        assertThat(entity.getSugars()).isEqualTo(20);
        assertThat(entity.getProtein()).isEqualTo(30);
        assertThat(entity.getSaturatedFat()).isEqualTo(5);
        assertThat(entity.getNatrium()).isEqualTo(800);
        assertThat(entity.getCarbohydrate()).isEqualTo(60);
        assertThat(entity.getCholesterol()).isEqualTo(70);
        assertThat(entity.getFat()).isEqualTo(15);
        assertThat(entity.getTransFat()).isEqualTo(1);
        assertThat(entity.getCaffeine()).isEqualTo(90);
        assertThat(entity.isSetMenu()).isEqualTo(true);
    }

    @Test
    @DisplayName("엔티티 → ProductNutrition 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductNutritionJpaEntity entity = ProductNutritionJpaEntity.create(
            42L,
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
            true
        );
        ReflectionTestUtils.setField(entity, "id", 41L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 3, 10, 3));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 4, 10, 4));

        ProductNutrition restored = ProductNutritionMapper.toDomain(entity);

        ProductNutrition expected = ProductNutrition.reconstitute(
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
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
