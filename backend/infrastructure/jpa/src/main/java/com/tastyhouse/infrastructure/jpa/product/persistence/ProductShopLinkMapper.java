package com.tastyhouse.infrastructure.jpa.product.persistence;

import com.tastyhouse.domain.product.model.ProductShopLink;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ProductShopLinkMapper {

    private ProductShopLinkMapper() {
    }

    static ProductShopLink toDomain(ProductShopLinkJpaEntity entity) {
        return ProductShopLink.reconstitute(
            entity.getId(),
            entity.getProductId() == null ? null : ProductId.of(entity.getProductId()),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getProductCategoryId() == null ? null : ProductCategoryId.of(entity.getProductCategoryId()),
            entity.getSort()
        );
    }

    static ProductShopLinkJpaEntity toEntity(ProductShopLink link) {
        return ProductShopLinkJpaEntity.create(
            link.getProductId() == null ? null : link.getProductId().value(),
            link.getShopId() == null ? null : link.getShopId().value(),
            link.getProductCategoryId() == null ? null : link.getProductCategoryId().value(),
            link.getSort()
        );
    }

    static void applyChanges(ProductShopLinkJpaEntity entity, ProductShopLink link) {
        entity.applyChanges(
            link.getProductCategoryId() == null ? null : link.getProductCategoryId().value(),
            link.getSort()
        );
    }
}
