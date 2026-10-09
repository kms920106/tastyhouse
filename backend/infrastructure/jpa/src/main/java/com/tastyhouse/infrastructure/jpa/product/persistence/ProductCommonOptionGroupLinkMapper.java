package com.tastyhouse.infrastructure.jpa.product.persistence;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

final class ProductCommonOptionGroupLinkMapper {

    private ProductCommonOptionGroupLinkMapper() {
    }

    static ProductCommonOptionGroupLink toDomain(ProductCommonOptionGroupLinkJpaEntity entity) {
        return ProductCommonOptionGroupLink.reconstitute(
            entity.getId(),
            entity.getProductId() == null ? null : ProductId.of(entity.getProductId()),
            entity.getOptionGroupId() == null ? null : ProductOptionGroupId.of(entity.getOptionGroupId()),
            entity.getSort()
        );
    }

    static ProductCommonOptionGroupLinkJpaEntity toEntity(ProductCommonOptionGroupLink link) {
        return ProductCommonOptionGroupLinkJpaEntity.create(
            link.getProductId() == null ? null : link.getProductId().value(),
            link.getOptionGroupId() == null ? null : link.getOptionGroupId().value(),
            link.getSort()
        );
    }

    static void applyChanges(ProductCommonOptionGroupLinkJpaEntity entity, ProductCommonOptionGroupLink link) {
        entity.applyChanges(link.getSort());
    }
}
