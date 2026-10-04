package com.tastyhouse.infrastructure.persistence.review.persistence;

import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.domain.review.model.ShopReviewDisplaySetting;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopReviewDisplaySettingMapper {

    private ShopReviewDisplaySettingMapper() {
    }

    static ShopReviewDisplaySetting toDomain(ShopReviewDisplaySettingJpaEntity entity) {
        return ShopReviewDisplaySetting.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getSortType() == null ? null : ReviewSortType.valueOf(entity.getSortType()),
            entity.getUpdatedAt()
        );
    }

    static ShopReviewDisplaySettingJpaEntity toEntity(ShopReviewDisplaySetting setting) {
        return ShopReviewDisplaySettingJpaEntity.create(
            setting.getShopId() == null ? null : setting.getShopId().value(),
            setting.getSortType() == null ? null : setting.getSortType().name()
        );
    }

    static void applyChanges(ShopReviewDisplaySettingJpaEntity entity, ShopReviewDisplaySetting setting) {
        entity.applyChanges(setting.getSortType() == null ? null : setting.getSortType().name());
    }
}
