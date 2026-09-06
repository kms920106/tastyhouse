package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopSuspensionMapper {
    private ShopSuspensionMapper() {
    }

    static ShopSuspension toDomain(ShopSuspensionJpaEntity entity) {
        return ShopSuspension.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getReason(),
            entity.getOrderMethod(),
            entity.getStartAt(),
            entity.getEndAt(),
            entity.getReleasedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopSuspensionJpaEntity toEntity(ShopSuspension domain) {
        return ShopSuspensionJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getReason(),
            domain.getOrderMethod(),
            domain.getStartAt(),
            domain.getEndAt(),
            domain.getReleasedAt()
        );
    }

    static void applyChanges(ShopSuspensionJpaEntity entity, ShopSuspension domain) {
        entity.applyChanges(domain.getReleasedAt());
    }
}
