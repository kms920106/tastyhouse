package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ProductCategoryMapper {
    private ProductCategoryMapper() {
    }

    static ProductCategory toDomain(ProductCategoryJpaEntity entity) {
        return ProductCategory.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getName(),
            entity.getDescription(),
            entity.getSort(),
            entity.isVisible()
        );
    }

    static ProductCategoryJpaEntity toEntity(ProductCategory category) {
        return ProductCategoryJpaEntity.create(
            category.getShopId() == null ? null : category.getShopId().value(),
            category.getName(),
            category.getDescription(),
            category.getSort(),
            category.isVisible()
        );
    }

    static void applyChanges(ProductCategoryJpaEntity entity, ProductCategory category) {
        entity.applyChanges(
            category.getName(),
            category.getDescription(),
            category.getSort(),
            category.isVisible()
        );
    }
}
