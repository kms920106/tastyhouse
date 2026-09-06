package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductBbq;
import com.tastyhouse.domain.product.vo.BbqCategoryId;
import com.tastyhouse.domain.product.vo.BbqMenuId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductBbqMapper {
    private ProductBbqMapper() {
    }

    static ProductBbq toDomain(ProductBbqJpaEntity entity) {
        return ProductBbq.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            IdMapping.vo(entity.getBbqMenuId(), BbqMenuId::of),
            IdMapping.vo(entity.getBbqCategoryId(), BbqCategoryId::of),
            entity.isOptionsSynced()
        );
    }

    static ProductBbqJpaEntity toEntity(ProductBbq domain) {
        return ProductBbqJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            IdMapping.raw(domain.getBbqMenuId(), BbqMenuId::value),
            IdMapping.raw(domain.getBbqCategoryId(), BbqCategoryId::value),
            domain.isOptionsSynced()
        );
    }

    static void applyChanges(ProductBbqJpaEntity entity, ProductBbq domain) {
        entity.applyChanges(domain.isOptionsSynced());
    }
}
