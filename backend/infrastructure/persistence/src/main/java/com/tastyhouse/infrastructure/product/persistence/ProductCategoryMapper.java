package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductCategoryMapper {
    private ProductCategoryMapper() {
    }

    static ProductCategory toDomain(ProductCategoryJpaEntity entity) {
        return ProductCategory.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getName(),
            entity.getDescription(),
            entity.getSort(),
            entity.isVisible()
        );
    }

    static ProductCategoryJpaEntity toEntity(ProductCategory domain) {
        return ProductCategoryJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getName(),
            domain.getDescription(),
            domain.getSort(),
            domain.isVisible()
        );
    }

    static void applyChanges(ProductCategoryJpaEntity entity, ProductCategory domain) {
        entity.applyChanges(
            domain.getName(),
            domain.getDescription(),
            domain.getSort(),
            domain.isVisible()
        );
    }
}
