package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopPhoneNumber;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopPhoneNumberMapper {
    private ShopPhoneNumberMapper() {
    }

    static ShopPhoneNumber toDomain(ShopPhoneNumberJpaEntity entity) {
        return ShopPhoneNumber.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getPhoneNumber(),
            entity.isPrimary(),
            entity.isVirtual(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopPhoneNumberJpaEntity toEntity(ShopPhoneNumber domain) {
        return ShopPhoneNumberJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getPhoneNumber(),
            domain.isPrimary(),
            domain.isVirtual()
        );
    }

    static void applyChanges(ShopPhoneNumberJpaEntity entity, ShopPhoneNumber domain) {
        entity.applyChanges(domain.isPrimary());
    }
}
