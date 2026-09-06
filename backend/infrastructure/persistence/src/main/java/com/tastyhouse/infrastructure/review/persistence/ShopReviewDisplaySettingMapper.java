package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.review.model.ShopReviewDisplaySetting;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopReviewDisplaySettingMapper {
    private ShopReviewDisplaySettingMapper() {
    }

    static ShopReviewDisplaySetting toDomain(ShopReviewDisplaySettingJpaEntity entity) {
        return ShopReviewDisplaySetting.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getSortType(),
            entity.getUpdatedAt()
        );
    }

    static ShopReviewDisplaySettingJpaEntity toEntity(ShopReviewDisplaySetting domain) {
        return ShopReviewDisplaySettingJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getSortType()
        );
    }

    static void applyChanges(ShopReviewDisplaySettingJpaEntity entity, ShopReviewDisplaySetting domain) {
        entity.applyChanges(domain.getSortType());
    }
}
