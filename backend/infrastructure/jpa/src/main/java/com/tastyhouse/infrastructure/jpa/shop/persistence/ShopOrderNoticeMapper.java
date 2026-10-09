package com.tastyhouse.infrastructure.jpa.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopOrderNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopOrderNoticeId;

final class ShopOrderNoticeMapper {

    private ShopOrderNoticeMapper() {
    }

    static ShopOrderNotice toDomain(ShopOrderNoticeJpaEntity entity) {
        return ShopOrderNotice.reconstitute(
            entity.getId() == null ? null : ShopOrderNoticeId.of(entity.getId()),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getContent(),
            entity.isHidden(),
            entity.getHiddenReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopOrderNoticeJpaEntity toEntity(ShopOrderNotice shopOrderNotice) {
        return ShopOrderNoticeJpaEntity.create(
            shopOrderNotice.getShopId() == null ? null : shopOrderNotice.getShopId().value(),
            shopOrderNotice.getContent(),
            shopOrderNotice.isHidden(),
            shopOrderNotice.getHiddenReason()
        );
    }

    static void applyChanges(ShopOrderNoticeJpaEntity entity, ShopOrderNotice shopOrderNotice) {
        entity.applyChanges(
            shopOrderNotice.getContent(),
            shopOrderNotice.isHidden(),
            shopOrderNotice.getHiddenReason()
        );
    }
}
