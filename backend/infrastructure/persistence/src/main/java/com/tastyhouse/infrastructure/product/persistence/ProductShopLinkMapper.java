package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductShopLink;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 메뉴-가게 연결 도메인 모델 ↔ JPA 엔티티 변환기.
 */
final class ProductShopLinkMapper {

    private ProductShopLinkMapper() {
    }

    static ProductShopLink toDomain(ProductShopLinkJpaEntity entity) {
        return ProductShopLink.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getProductCategoryId(), ProductCategoryId::of),
            entity.getSort()
        );
    }

    static ProductShopLinkJpaEntity toEntity(ProductShopLink domain) {
        return ProductShopLinkJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getProductCategoryId(), ProductCategoryId::value),
            domain.getSort()
        );
    }

    static void applyChanges(ProductShopLinkJpaEntity entity, ProductShopLink domain) {
        entity.applyChanges(
            IdMapping.raw(domain.getProductCategoryId(), ProductCategoryId::value),
            domain.getSort()
        );
    }
}
