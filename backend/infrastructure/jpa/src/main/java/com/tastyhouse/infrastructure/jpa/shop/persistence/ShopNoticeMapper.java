package com.tastyhouse.infrastructure.jpa.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopNoticeMapper {

    private ShopNoticeMapper() {
    }

    static ShopNotice toDomain(ShopNoticeJpaEntity entity) {
        return ShopNotice.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getContent(),
            entity.isExposed(),
            entity.isHidden(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopNoticeJpaEntity toEntity(ShopNotice shopNotice) {
        return ShopNoticeJpaEntity.create(
            shopNotice.getShopId() == null ? null : shopNotice.getShopId().value(),
            shopNotice.getContent(),
            shopNotice.isExposed(),
            shopNotice.isHidden()
        );
    }

    static void applyChanges(ShopNoticeJpaEntity entity, ShopNotice shopNotice) {
        entity.applyChanges(
            shopNotice.getContent(),
            shopNotice.isExposed(),
            shopNotice.isHidden()
        );
    }
}
