package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shop.model.ShopBookmark;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopBookmarkMapper {

    private ShopBookmarkMapper() {
    }

    static ShopBookmark toDomain(ShopBookmarkJpaEntity entity) {
        return ShopBookmark.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getMemberId() == null ? null : MemberId.of(entity.getMemberId())
        );
    }

    static ShopBookmarkJpaEntity toEntity(ShopBookmark shopBookmark) {
        return ShopBookmarkJpaEntity.create(
            shopBookmark.getShopId() == null ? null : shopBookmark.getShopId().value(),
            shopBookmark.getMemberId() == null ? null : shopBookmark.getMemberId().value()
        );
    }
}
