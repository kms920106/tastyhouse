package com.tastyhouse.infrastructure.persistence.product.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCommonOptionGroupLinkMapperTest {

    @Test
    @DisplayName("ProductCommonOptionGroupLink → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductCommonOptionGroupLink productCommonOptionGroupLink = ProductCommonOptionGroupLink.reconstitute(
            191L,
            ProductId.of(192L),
            ProductOptionGroupId.of(193L),
            11
        );

        ProductCommonOptionGroupLinkJpaEntity entity = ProductCommonOptionGroupLinkMapper.toEntity(productCommonOptionGroupLink);

        assertThat(entity.getProductId()).isEqualTo(192L);
        assertThat(entity.getOptionGroupId()).isEqualTo(193L);
        assertThat(entity.getSort()).isEqualTo(11);
    }

    @Test
    @DisplayName("엔티티 → ProductCommonOptionGroupLink 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductCommonOptionGroupLinkJpaEntity entity = ProductCommonOptionGroupLinkJpaEntity.create(
            192L,
            193L,
            11
        );
        ReflectionTestUtils.setField(entity, "id", 191L);

        ProductCommonOptionGroupLink restored = ProductCommonOptionGroupLinkMapper.toDomain(entity);

        ProductCommonOptionGroupLink expected = ProductCommonOptionGroupLink.reconstitute(
            191L,
            ProductId.of(192L),
            ProductOptionGroupId.of(193L),
            11
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
