package com.tastyhouse.infrastructure.product.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductOptionGroupLinkMapperTest {

    @Test
    @DisplayName("ProductOptionGroupLink → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductOptionGroupLink productOptionGroupLink = ProductOptionGroupLink.reconstitute(
            181L,
            ProductId.of(182L),
            ProductOptionGroupId.of(183L),
            10
        );

        ProductOptionGroupLinkJpaEntity entity = ProductOptionGroupLinkMapper.toEntity(productOptionGroupLink);

        assertThat(entity.getProductId()).isEqualTo(182L);
        assertThat(entity.getOptionGroupId()).isEqualTo(183L);
        assertThat(entity.getSort()).isEqualTo(10);
    }

    @Test
    @DisplayName("엔티티 → ProductOptionGroupLink 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductOptionGroupLinkJpaEntity entity = ProductOptionGroupLinkJpaEntity.create(
            182L,
            183L,
            10
        );
        ReflectionTestUtils.setField(entity, "id", 181L);

        ProductOptionGroupLink restored = ProductOptionGroupLinkMapper.toDomain(entity);

        ProductOptionGroupLink expected = ProductOptionGroupLink.reconstitute(
            181L,
            ProductId.of(182L),
            ProductOptionGroupId.of(183L),
            10
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
