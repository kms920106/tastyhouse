package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductBbq;
import com.tastyhouse.domain.product.vo.BbqCategoryId;
import com.tastyhouse.domain.product.vo.BbqMenuId;
import com.tastyhouse.domain.product.vo.ProductId;

final class ProductBbqMapper {
    private ProductBbqMapper() {
    }

    static ProductBbq toDomain(ProductBbqJpaEntity entity) {
        return ProductBbq.reconstitute(
            entity.getId(),
            entity.getProductId() == null ? null : ProductId.of(entity.getProductId()),
            entity.getBbqMenuId() == null ? null : BbqMenuId.of(entity.getBbqMenuId()),
            entity.getBbqCategoryId() == null ? null : BbqCategoryId.of(entity.getBbqCategoryId()),
            entity.isOptionsSynced()
        );
    }

    static ProductBbqJpaEntity toEntity(ProductBbq productBbq) {
        return ProductBbqJpaEntity.create(
            productBbq.getProductId() == null ? null : productBbq.getProductId().value(),
            productBbq.getBbqMenuId() == null ? null : productBbq.getBbqMenuId().value(),
            productBbq.getBbqCategoryId() == null ? null : productBbq.getBbqCategoryId().value(),
            productBbq.isOptionsSynced()
        );
    }

    static void applyChanges(ProductBbqJpaEntity entity, ProductBbq productBbq) {
        entity.applyChanges(productBbq.isOptionsSynced());
    }
}
