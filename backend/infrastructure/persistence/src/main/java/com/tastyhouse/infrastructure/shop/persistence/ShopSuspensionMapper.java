package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.SuspensionReason;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopSuspensionMapper {

    private ShopSuspensionMapper() {
    }

    static ShopSuspension toDomain(ShopSuspensionJpaEntity entity) {
        return ShopSuspension.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getReason() == null ? null : SuspensionReason.valueOf(entity.getReason()),
            entity.getOrderMethod() == null ? null : OrderMethod.valueOf(entity.getOrderMethod()),
            entity.getStartAt(),
            entity.getEndAt(),
            entity.getReleasedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopSuspensionJpaEntity toEntity(ShopSuspension shopSuspension) {
        return ShopSuspensionJpaEntity.create(
            shopSuspension.getShopId() == null ? null : shopSuspension.getShopId().value(),
            shopSuspension.getReason() == null ? null : shopSuspension.getReason().name(),
            shopSuspension.getOrderMethod() == null ? null : shopSuspension.getOrderMethod().name(),
            shopSuspension.getStartAt(),
            shopSuspension.getEndAt(),
            shopSuspension.getReleasedAt()
        );
    }

    static void applyChanges(ShopSuspensionJpaEntity entity, ShopSuspension shopSuspension) {
        entity.applyChanges(shopSuspension.getReleasedAt());
    }
}
