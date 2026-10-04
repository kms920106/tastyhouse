package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCommonOptionMapperTest {

    @Test
    @DisplayName("ProductCommonOption → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductCommonOption productCommonOption = ProductCommonOption.reconstitute(
            151L,
            ProductOptionGroupId.of(152L),
            "시럽",
            700,
            7,
            true,
            LocalDateTime.of(2026, 2, 20, 10, 20),
            false
        );

        ProductCommonOptionJpaEntity entity = ProductCommonOptionMapper.toEntity(productCommonOption);

        assertThat(entity.getOptionGroupId()).isEqualTo(152L);
        assertThat(entity.getName()).isEqualTo("시럽");
        assertThat(entity.getAdditionalPrice()).isEqualTo(700);
        assertThat(entity.getSort()).isEqualTo(7);
        assertThat(entity.isSoldOut()).isEqualTo(true);
        assertThat(entity.getSoldOutUntil()).isEqualTo(LocalDateTime.of(2026, 2, 20, 10, 20));
        assertThat(entity.isVisible()).isEqualTo(false);
    }

    @Test
    @DisplayName("엔티티 → ProductCommonOption 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductCommonOptionJpaEntity entity = ProductCommonOptionJpaEntity.create(
            152L,
            "시럽",
            700,
            7,
            true,
            LocalDateTime.of(2026, 2, 20, 10, 20),
            false
        );
        ReflectionTestUtils.setField(entity, "id", 151L);

        ProductCommonOption restored = ProductCommonOptionMapper.toDomain(entity);

        ProductCommonOption expected = ProductCommonOption.reconstitute(
            151L,
            ProductOptionGroupId.of(152L),
            "시럽",
            700,
            7,
            true,
            LocalDateTime.of(2026, 2, 20, 10, 20),
            false
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
