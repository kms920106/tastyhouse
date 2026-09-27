package com.tastyhouse.application.product.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductShopLink;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductShopLinkStateMapperTest {

    @Test
    @DisplayName("ProductShopLink → ProductShopLinkState → ProductShopLink 왕복 시 모든 필드가 보존된다")
    void productShopLinkRoundTrip() {
        ProductShopLink original = ProductShopLink.reconstitute(
            121L,
            ProductId.of(122L),
            ShopId.of(123L),
            ProductCategoryId.of(124L),
            5
        );

        ProductShopLink restored = ProductShopLinkStateMapper.toDomain(ProductShopLinkStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
