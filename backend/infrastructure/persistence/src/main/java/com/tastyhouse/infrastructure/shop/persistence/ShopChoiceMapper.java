package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopChoice;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopChoiceMapper {

    private ShopChoiceMapper() {
    }

    static ShopChoice toDomain(ShopChoiceJpaEntity entity) {
        return ShopChoice.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getTitle(),
            entity.getContent()
        );
    }

    static ShopChoiceJpaEntity toEntity(ShopChoice shopChoice) {
        return ShopChoiceJpaEntity.create(
            shopChoice.getShopId() == null ? null : shopChoice.getShopId().value(),
            shopChoice.getTitle(),
            shopChoice.getContent()
        );
    }

    static void applyChanges(ShopChoiceJpaEntity entity, ShopChoice shopChoice) {
        entity.applyChanges(
            shopChoice.getTitle(),
            shopChoice.getContent()
        );
    }
}
