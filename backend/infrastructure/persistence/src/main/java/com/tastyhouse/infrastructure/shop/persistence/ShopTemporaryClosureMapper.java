package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopTemporaryClosureState;

final class ShopTemporaryClosureMapper {
    private ShopTemporaryClosureMapper() {
    }

    static ShopTemporaryClosureState toState(ShopTemporaryClosureJpaEntity entity) {
        return new ShopTemporaryClosureState(
            entity.getId(),
            entity.getShopId(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getCreatedAt()
        );
    }

    static ShopTemporaryClosureJpaEntity toEntity(ShopTemporaryClosureState state) {
        return ShopTemporaryClosureJpaEntity.create(
            state.shopId(),
            state.startDate(),
            state.endDate()
        );
    }
}
