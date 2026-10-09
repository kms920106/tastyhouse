package com.tastyhouse.infrastructure.jpa.product.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.product.model.AllergenType;
import com.tastyhouse.domain.product.model.ProductAllergen;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductAllergenMapperTest {

    @Test
    @DisplayName("ProductAllergen → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductAllergen productAllergen = ProductAllergen.reconstitute(
            11L,
            ProductId.of(12L),
            AllergenType.PEANUT,
            LocalDateTime.of(2026, 2, 1, 10, 1),
            LocalDateTime.of(2026, 2, 2, 10, 2)
        );

        ProductAllergenJpaEntity entity = ProductAllergenMapper.toEntity(productAllergen);

        assertThat(entity.getProductId()).isEqualTo(12L);
        assertThat(entity.getAllergenType()).isEqualTo("PEANUT");
    }

    @Test
    @DisplayName("엔티티 → ProductAllergen 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductAllergenJpaEntity entity = ProductAllergenJpaEntity.create(
            12L,
            "PEANUT"
        );
        ReflectionTestUtils.setField(entity, "id", 11L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 1, 10, 1));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 2, 10, 2));

        ProductAllergen restored = ProductAllergenMapper.toDomain(entity);

        ProductAllergen expected = ProductAllergen.reconstitute(
            11L,
            ProductId.of(12L),
            AllergenType.PEANUT,
            LocalDateTime.of(2026, 2, 1, 10, 1),
            LocalDateTime.of(2026, 2, 2, 10, 2)
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
