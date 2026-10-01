package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

final class ProductOptionGroupLinkMapper {

    private ProductOptionGroupLinkMapper() {
    }

    static ProductOptionGroupLink toDomain(ProductOptionGroupLinkJpaEntity entity) {
        return ProductOptionGroupLink.reconstitute(
            entity.getId(),
            entity.getProductId() == null ? null : ProductId.of(entity.getProductId()),
            entity.getOptionGroupId() == null ? null : ProductOptionGroupId.of(entity.getOptionGroupId()),
            entity.getSort()
        );
    }

    static ProductOptionGroupLinkJpaEntity toEntity(ProductOptionGroupLink link) {
        return ProductOptionGroupLinkJpaEntity.create(
            link.getProductId() == null ? null : link.getProductId().value(),
            link.getOptionGroupId() == null ? null : link.getOptionGroupId().value(),
            link.getSort()
        );
    }

    static void applyChanges(ProductOptionGroupLinkJpaEntity entity, ProductOptionGroupLink link) {
        entity.applyChanges(link.getSort());
    }
}
