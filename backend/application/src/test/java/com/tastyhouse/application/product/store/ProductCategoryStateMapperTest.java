package com.tastyhouse.application.product.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCategoryStateMapperTest {

    @Test
    @DisplayName("ProductCategory → ProductCategoryState → ProductCategory 왕복 시 모든 필드가 보존된다")
    void productCategoryRoundTrip() {
        ProductCategory original = ProductCategory.reconstitute(
            71L,
            ShopId.of(72L),
            "메인",
            "대표 메뉴",
            3,
            true
        );

        ProductCategory restored = ProductCategoryStateMapper.toDomain(ProductCategoryStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
