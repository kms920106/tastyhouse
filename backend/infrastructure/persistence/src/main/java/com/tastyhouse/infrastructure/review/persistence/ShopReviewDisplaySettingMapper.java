package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.application.review.port.out.write.ShopReviewDisplaySettingState;

final class ShopReviewDisplaySettingMapper {
    private ShopReviewDisplaySettingMapper() {
    }

    static ShopReviewDisplaySettingState toState(ShopReviewDisplaySettingJpaEntity entity) {
        return new ShopReviewDisplaySettingState(
            entity.getId(),
            entity.getShopId(),
            entity.getSortType(),
            entity.getUpdatedAt()
        );
    }

    static ShopReviewDisplaySettingJpaEntity toEntity(ShopReviewDisplaySettingState state) {
        return ShopReviewDisplaySettingJpaEntity.create(
            state.shopId(),
            state.sortType()
        );
    }

    static void applyChanges(ShopReviewDisplaySettingJpaEntity entity, ShopReviewDisplaySettingState state) {
        entity.applyChanges(state.sortType());
    }
}
