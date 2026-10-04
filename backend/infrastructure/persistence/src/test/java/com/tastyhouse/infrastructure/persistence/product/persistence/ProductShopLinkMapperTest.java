package com.tastyhouse.infrastructure.persistence.product.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.product.model.ProductShopLink;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductShopLinkMapperTest {

    @Test
    @DisplayName("ProductShopLink → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ProductShopLink productShopLink = ProductShopLink.reconstitute(
            121L,
            ProductId.of(122L),
            ShopId.of(123L),
            ProductCategoryId.of(124L),
            5
        );

        ProductShopLinkJpaEntity entity = ProductShopLinkMapper.toEntity(productShopLink);

        assertThat(entity.getProductId()).isEqualTo(122L);
        assertThat(entity.getShopId()).isEqualTo(123L);
        assertThat(entity.getProductCategoryId()).isEqualTo(124L);
        assertThat(entity.getSort()).isEqualTo(5);
    }

    @Test
    @DisplayName("엔티티 → ProductShopLink 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ProductShopLinkJpaEntity entity = ProductShopLinkJpaEntity.create(
            122L,
            123L,
            124L,
            5
        );
        ReflectionTestUtils.setField(entity, "id", 121L);

        ProductShopLink restored = ProductShopLinkMapper.toDomain(entity);

        ProductShopLink expected = ProductShopLink.reconstitute(
            121L,
            ProductId.of(122L),
            ShopId.of(123L),
            ProductCategoryId.of(124L),
            5
        );
        assertThat(restored).usingRecursiveComparison().isEqualTo(expected);
    }
}
