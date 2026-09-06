package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductOptionGroupLinkMapper {
    private ProductOptionGroupLinkMapper() {
    }

    static ProductOptionGroupLink toDomain(ProductOptionGroupLinkJpaEntity entity) {
        return ProductOptionGroupLink.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            IdMapping.vo(entity.getOptionGroupId(), ProductOptionGroupId::of),
            entity.getSort()
        );
    }

    static ProductOptionGroupLinkJpaEntity toEntity(ProductOptionGroupLink domain) {
        return ProductOptionGroupLinkJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            IdMapping.raw(domain.getOptionGroupId(), ProductOptionGroupId::value),
            domain.getSort()
        );
    }

    static void applyChanges(ProductOptionGroupLinkJpaEntity entity, ProductOptionGroupLink domain) {
        entity.applyChanges(domain.getSort());
    }
}
