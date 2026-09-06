package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopOrderNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopOrderNoticeId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopOrderNoticeMapper {
    private ShopOrderNoticeMapper() {
    }

    static ShopOrderNotice toDomain(ShopOrderNoticeJpaEntity entity) {
        return ShopOrderNotice.reconstitute(
            IdMapping.vo(entity.getId(), ShopOrderNoticeId::of),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getContent(),
            entity.isHidden(),
            entity.getHiddenReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopOrderNoticeJpaEntity toEntity(ShopOrderNotice domain) {
        return ShopOrderNoticeJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getContent(),
            domain.isHidden(),
            domain.getHiddenReason()
        );
    }

    static void applyChanges(ShopOrderNoticeJpaEntity entity, ShopOrderNotice domain) {
        entity.applyChanges(
            domain.getContent(),
            domain.isHidden(),
            domain.getHiddenReason()
        );
    }
}
