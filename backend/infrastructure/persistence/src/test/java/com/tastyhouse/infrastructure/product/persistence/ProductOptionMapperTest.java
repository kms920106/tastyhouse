package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductOptionMapperTest {

    @Test
    @DisplayName("ProductOption → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductOption productOption = ProductOption.reconstitute(
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

        ProductOptionJpaEntity entity = ProductOptionMapper.toEntity(productOption);

        assertThat(entity.getOptionGroupId()).isEqualTo(142L);
        assertThat(entity.getName()).isEqualTo("샷 추가");
        assertThat(entity.getAdditionalPrice()).isEqualTo(500);
        assertThat(entity.getSort()).isEqualTo(6);
        assertThat(entity.isSoldOut()).isEqualTo(true);
        assertThat(entity.getSoldOutUntil()).isEqualTo(LocalDateTime.of(2026, 2, 19, 10, 19));
        assertThat(entity.isVisible()).isEqualTo(false);
        assertThat(entity.getCupCount()).isEqualTo(2);
        assertThat(entity.getPersonalCupDiscountAmount()).isEqualTo(300);
    }

    @Test
    @DisplayName("엔티티 → ProductOption 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductOptionJpaEntity entity = ProductOptionJpaEntity.create(
            142L,
            "샷 추가",
            500,
            6,
            true,
            LocalDateTime.of(2026, 2, 19, 10, 19),
            false,
            2,
            300
        );
        ReflectionTestUtils.setField(entity, "id", 141L);

        ProductOption restored = ProductOptionMapper.toDomain(entity);

        ProductOption expected = ProductOption.reconstitute(
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
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
