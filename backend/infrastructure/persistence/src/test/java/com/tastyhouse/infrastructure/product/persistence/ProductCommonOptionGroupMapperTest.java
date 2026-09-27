package com.tastyhouse.infrastructure.product.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroup;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCommonOptionGroupMapperTest {

    @Test
    @DisplayName("ProductCommonOptionGroup → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductCommonOptionGroup productCommonOptionGroup = ProductCommonOptionGroup.reconstitute(
            171L,
            ProductId.of(172L),
            "토핑",
            "공통 토핑",
            false,
            true,
            0,
            4,
            9,
            true
        );

        ProductCommonOptionGroupJpaEntity entity = ProductCommonOptionGroupMapper.toEntity(productCommonOptionGroup);

        assertThat(entity.getProductId()).isEqualTo(172L);
        assertThat(entity.getName()).isEqualTo("토핑");
        assertThat(entity.getDescription()).isEqualTo("공통 토핑");
        assertThat(entity.isRequired()).isEqualTo(false);
        assertThat(entity.isMultipleSelect()).isEqualTo(true);
        assertThat(entity.getMinSelect()).isEqualTo(0);
        assertThat(entity.getMaxSelect()).isEqualTo(4);
        assertThat(entity.getSort()).isEqualTo(9);
        assertThat(entity.isVisible()).isEqualTo(true);
    }

    @Test
    @DisplayName("엔티티 → ProductCommonOptionGroup 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductCommonOptionGroupJpaEntity entity = ProductCommonOptionGroupJpaEntity.create(
            172L,
            "토핑",
            "공통 토핑",
            false,
            true,
            0,
            4,
            9,
            true
        );
        ReflectionTestUtils.setField(entity, "id", 171L);

        ProductCommonOptionGroup restored = ProductCommonOptionGroupMapper.toDomain(entity);

        ProductCommonOptionGroup expected = ProductCommonOptionGroup.reconstitute(
            171L,
            ProductId.of(172L),
            "토핑",
            "공통 토핑",
            false,
            true,
            0,
            4,
            9,
            true
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
