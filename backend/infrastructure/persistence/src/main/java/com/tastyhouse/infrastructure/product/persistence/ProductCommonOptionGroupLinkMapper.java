package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductCommonOptionGroupLinkMapper {
    private ProductCommonOptionGroupLinkMapper() {
    }

    static ProductCommonOptionGroupLink toDomain(ProductCommonOptionGroupLinkJpaEntity entity) {
        return ProductCommonOptionGroupLink.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            IdMapping.vo(entity.getOptionGroupId(), ProductOptionGroupId::of),
            entity.getSort()
        );
    }

    static ProductCommonOptionGroupLinkJpaEntity toEntity(ProductCommonOptionGroupLink domain) {
        return ProductCommonOptionGroupLinkJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            IdMapping.raw(domain.getOptionGroupId(), ProductOptionGroupId::value),
            domain.getSort()
        );
    }

    static void applyChanges(ProductCommonOptionGroupLinkJpaEntity entity, ProductCommonOptionGroupLink domain) {
        entity.applyChanges(domain.getSort());
    }
}
