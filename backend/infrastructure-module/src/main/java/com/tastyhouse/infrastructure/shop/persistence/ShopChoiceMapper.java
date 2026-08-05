package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopChoice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopChoiceMapper {

    private ShopChoiceMapper() {
    }

    static ShopChoice toDomain(ShopChoiceJpaEntity entity) {
        return ShopChoice.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getTitle(),
            entity.getContent()
        );
    }

    static ShopChoiceJpaEntity toEntity(ShopChoice domain) {
        return ShopChoiceJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getTitle(),
            domain.getContent()
        );
    }

    static void applyChanges(ShopChoiceJpaEntity entity, ShopChoice domain) {
        entity.applyChanges(
            domain.getTitle(),
            domain.getContent()
        );
    }
}
