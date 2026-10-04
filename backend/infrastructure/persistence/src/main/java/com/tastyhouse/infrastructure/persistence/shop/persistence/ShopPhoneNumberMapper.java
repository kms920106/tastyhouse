package com.tastyhouse.infrastructure.persistence.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopPhoneNumber;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopPhoneNumberMapper {

    private ShopPhoneNumberMapper() {
    }

    static ShopPhoneNumber toDomain(ShopPhoneNumberJpaEntity entity) {
        return ShopPhoneNumber.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getPhoneNumber(),
            entity.isPrimary(),
            entity.isVirtual(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopPhoneNumberJpaEntity toEntity(ShopPhoneNumber shopPhoneNumber) {
        return ShopPhoneNumberJpaEntity.create(
            shopPhoneNumber.getShopId() == null ? null : shopPhoneNumber.getShopId().value(),
            shopPhoneNumber.getPhoneNumber(),
            shopPhoneNumber.isPrimary(),
            shopPhoneNumber.isVirtual()
        );
    }

    static void applyChanges(ShopPhoneNumberJpaEntity entity, ShopPhoneNumber shopPhoneNumber) {
        entity.applyChanges(shopPhoneNumber.isPrimary());
    }
}
