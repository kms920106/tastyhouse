package com.tastyhouse.application.product.store;

import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductCategoryState;

final class ProductCategoryStateMapper {
    private ProductCategoryStateMapper() {
    }

    static ProductCategory toDomain(ProductCategoryState state) {
        return ProductCategory.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.name(),
            state.description(),
            state.sort(),
            state.visible()
        );
    }

    static ProductCategoryState toState(ProductCategory category) {
        return new ProductCategoryState(
            category.getId(),
            category.getShopId() == null ? null : category.getShopId().value(),
            category.getName(),
            category.getDescription(),
            category.getSort(),
            category.isVisible()
        );
    }
}
