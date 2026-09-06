package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopNoticeMapper {
    private ShopNoticeMapper() {
    }

    static ShopNotice toDomain(ShopNoticeJpaEntity entity) {
        return ShopNotice.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getContent(),
            entity.isExposed(),
            entity.isHidden(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopNoticeJpaEntity toEntity(ShopNotice domain) {
        return ShopNoticeJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getContent(),
            domain.isExposed(),
            domain.isHidden()
        );
    }

    static void applyChanges(ShopNoticeJpaEntity entity, ShopNotice domain) {
        entity.applyChanges(
            domain.getContent(),
            domain.isExposed(),
            domain.isHidden()
        );
    }
}
