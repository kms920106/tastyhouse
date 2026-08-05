package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shop.model.ShopBookmark;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopBookmarkMapper {

    private ShopBookmarkMapper() {
    }

    static ShopBookmark toDomain(ShopBookmarkJpaEntity entity) {
        return ShopBookmark.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getMemberId(), MemberId::of)
        );
    }

    static ShopBookmarkJpaEntity toEntity(ShopBookmark domain) {
        return ShopBookmarkJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getMemberId(), MemberId::value)
        );
    }
}
