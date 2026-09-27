package com.tastyhouse.infrastructure.product.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductOptionGroupMapperTest {

    @Test
    @DisplayName("ProductOptionGroup → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductOptionGroup productOptionGroup = ProductOptionGroup.reconstitute(
            161L,
            ProductId.of(162L),
            "사이즈",
            "컵 크기",
            true,
            false,
            1,
            3,
            8,
            true,
            ProductOptionGroupType.CUP_DEPOSIT
        );

        ProductOptionGroupJpaEntity entity = ProductOptionGroupMapper.toEntity(productOptionGroup);

        assertThat(entity.getProductId()).isEqualTo(162L);
        assertThat(entity.getName()).isEqualTo("사이즈");
        assertThat(entity.getDescription()).isEqualTo("컵 크기");
        assertThat(entity.isRequired()).isEqualTo(true);
        assertThat(entity.isMultipleSelect()).isEqualTo(false);
        assertThat(entity.getMinSelect()).isEqualTo(1);
        assertThat(entity.getMaxSelect()).isEqualTo(3);
        assertThat(entity.getSort()).isEqualTo(8);
        assertThat(entity.isVisible()).isEqualTo(true);
        assertThat(entity.getGroupType()).isEqualTo("CUP_DEPOSIT");
    }

    @Test
    @DisplayName("엔티티 → ProductOptionGroup 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductOptionGroupJpaEntity entity = ProductOptionGroupJpaEntity.create(
            162L,
            "사이즈",
            "컵 크기",
            true,
            false,
            1,
            3,
            8,
            true,
            "CUP_DEPOSIT"
        );
        ReflectionTestUtils.setField(entity, "id", 161L);

        ProductOptionGroup restored = ProductOptionGroupMapper.toDomain(entity);

        ProductOptionGroup expected = ProductOptionGroup.reconstitute(
            161L,
            ProductId.of(162L),
            "사이즈",
            "컵 크기",
            true,
            false,
            1,
            3,
            8,
            true,
            ProductOptionGroupType.CUP_DEPOSIT
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
