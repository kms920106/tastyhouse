package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.domain.model.ShopChoice;

final class ShopChoiceMapper {

    private ShopChoiceMapper() {
    }

    static ShopChoice toDomain(ShopChoiceJpaEntity entity) {
        return ShopChoice.reconstitute(
            entity.getId(),
            entity.getShopId(),
            entity.getTitle(),
            entity.getContent()
        );
    }

    static ShopChoiceJpaEntity toEntity(ShopChoice domain) {
        return ShopChoiceJpaEntity.create(
            domain.getShopId(),
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
