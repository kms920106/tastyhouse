package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.core.domain.shop.domain.model.ShopBookmark;

final class ShopBookmarkMapper {

    private ShopBookmarkMapper() {
    }

    static ShopBookmark toDomain(ShopBookmarkJpaEntity entity) {
        return ShopBookmark.reconstitute(
            entity.getId(),
            entity.getShopId(),
            entity.getMemberId()
        );
    }

    static ShopBookmarkJpaEntity toEntity(ShopBookmark domain) {
        return ShopBookmarkJpaEntity.create(
            domain.getShopId(),
            domain.getMemberId()
        );
    }
}
